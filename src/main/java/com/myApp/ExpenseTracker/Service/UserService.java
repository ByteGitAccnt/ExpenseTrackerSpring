package com.myApp.ExpenseTracker.Service;


import com.myApp.ExpenseTracker.Dto.UserResponse;
import com.myApp.ExpenseTracker.Exeception.ResourceAlreadyExists;
import com.myApp.ExpenseTracker.Exeception.ResourceNotFoundException;
import com.myApp.ExpenseTracker.Req.RegisterRequest;
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
    public UserResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials for username:" + username));
        boolean success = BCrypt.checkpw(password, user.getPassword());
        if (!success) {
            throw new IllegalArgumentException("Invalid credentials , username or password incorrect");
        }
        logger.atInfo().log("Login successful for username {}", username);
        auditService.logSuccess(
                user.getId(),
                EntityType.USER,
                user.getId(),
                Status.LOGGED.name()
        );
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBalance());
    }
    @Transactional
    public UserResponse register(RegisterRequest dto){
        if(userRepository.existsByUsername(dto.getUsername())){
            throw new ResourceAlreadyExists("User already exists for user:" + dto.getUsername());
        }
        String hashedPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());// converts to hashed value
        User user = new User(
                dto.getName(),
                dto.getUsername(),
                hashedPassword,
                dto.getEmail()
        );
        User saved = userRepository.save(user);
        logger.atInfo().log("Registration successful for username {}", dto.getUsername());
        auditService.logSuccess(saved.getId(), EntityType.USER, saved.getId(), Status.REGISTERED.name());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail() , user.getBalance());
    }
    @Transactional
    public UserResponse addIncome(Long userid, BigDecimal amnt){
        User user = userRepository.findByIdForUpdate(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for user:" + userid));
        user.setBalance(user.getBalance().add(amnt));
        auditService.logUpdate(userid,EntityType.INCOME,userid,"Balance", amnt.toString());
        return new UserResponse(user.getId(), user.getUsername(),user.getEmail() ,user.getBalance());
    }
    @Transactional
    public Optional<User> getUserByid(Long userid){
        return userRepository.findById(userid);
    }
}
