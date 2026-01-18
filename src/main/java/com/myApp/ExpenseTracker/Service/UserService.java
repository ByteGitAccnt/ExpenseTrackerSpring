package com.myApp.ExpenseTracker.Service;


import com.myApp.ExpenseTracker.Dto.RegisterRequest;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
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
        userRepository.save(user);
        logger.atInfo().log("Registration successful for username {}", dto.getUsername());
        return Status.CREATED;
    }

}
