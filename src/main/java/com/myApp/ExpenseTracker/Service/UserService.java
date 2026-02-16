package com.myApp.ExpenseTracker.Service;


import com.myApp.ExpenseTracker.Dto.RegisterRequest;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuditService auditService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    public UserService(UserRepository userRepository,AuditService  audit ){
        this.userRepository = userRepository;
        this.auditService = audit;
    }
    @Transactional(readOnly = true)
    public boolean login(String username , String password){
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.atInfo().log("Login attempt failed for username {}", username);
            return false;
        }
        User user = userOpt.get();
        boolean success = BCrypt.checkpw(password, user.getPassword());
        logger.atInfo().log("Login attempt for username {} : {}",
                username, success ? "SUCCESS" : "FAILED");
        if(success){
            auditService.logSuccess(user.getId(), EntityType.USER,user.getId(),Status.LOGGED.name());
        }else{
            auditService.logFailure(user.getId(), EntityType.USER,user.getId(),Status.LOGIN_FAILED.name());
        }
        return success;
    }
    @Transactional
    public Status register(RegisterRequest dto){
        if(userRepository.existsByUsername(dto.getUsername())){
            logger.atWarn().log("User registration failed:username already exists! for username {}", dto.getUsername());
            return Status.USERNAME_EXISTS;
        }
        String hashedPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());// converts to hashed value
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(hashedPassword);
        user.setBalance(BigDecimal.valueOf(0.0));
        userRepository.save(user);
        logger.atInfo().log("Registration successful for username {}", dto.getUsername());
        Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
        userOpt.ifPresent(value -> auditService.logSuccess(value.getId(), EntityType.USER, value.getId(), Status.REGISTERED.name()));
        return Status.CREATED;
    }
    @Transactional
    public Status addIncome(Long userid, BigDecimal amnt){

        if(userRepository.increaseBalance(userid , amnt) > 0){
            logger.atInfo().log("Income added successful for user {}" , userid);
            auditService.logUpdate(userid,EntityType.INCOME,userid,"Balance", amnt.toString());
            return Status.SUCCESS;
        }
        logger.atWarn().log("Income adding failed for user {}" , userid);
        return Status.FAILED;
    }
}
