package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Req.AddMoneyRequest;
import com.myApp.ExpenseTracker.Req.LoginRequest;
import com.myApp.ExpenseTracker.Req.RegisterRequest;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.Status;
import com.myApp.ExpenseTracker.Service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    public AuthController(UserService userService,CurrentUserProvider provider) {
        this.userService = userService;
        this.currentUserProvider = provider;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        logger.atInfo().log("Login request received for username={}", request.getUsername());
        boolean success = userService.login(
                request.getUsername(),
                request.getPassword()
        );
        if (success) {
            logger.atInfo().log("Login successful for username={}", request.getUsername());
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Login failed for username={}", request.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req){
        logger.atInfo().log("Register request received for username={}", req.getUsername());
        if(!req.getConfirmPassword().equals(req.getPassword())){
            return ResponseEntity.badRequest().body("Incorrect Password!");
        }
        Status status = userService.register(req);
        if(status == Status.CREATED){
            logger.atInfo().log("Register successful for username={}", req.getUsername());
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Register failed for username={}", req.getUsername());
        return ResponseEntity.badRequest().body(status.name());
    }
    @PostMapping("/income")
    public  ResponseEntity<?> addIncome(@Valid @RequestBody AddMoneyRequest req){
        logger.atInfo().log("Add Income request received. ");
        Long userid = currentUserProvider.getCurrentUserId();
        Status status = userService.addIncome(userid, req.getAmount());
        if(status == Status.SUCCESS){
            logger.atInfo().log("Income added. ");
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body(status.name());
    }
}
