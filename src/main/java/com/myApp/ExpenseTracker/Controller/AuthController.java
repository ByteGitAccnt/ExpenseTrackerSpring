package com.myApp.ExpenseTracker.Controller;


import com.myApp.ExpenseTracker.Dto.UserResponse;
import com.myApp.ExpenseTracker.Req.AddMoneyRequest;
import com.myApp.ExpenseTracker.Req.LoginRequest;
import com.myApp.ExpenseTracker.Req.RegisterRequest;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.atInfo().log("Login request received for username={}", request.getUsername());
        UserResponse response  = userService.login(
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req){
        logger.atInfo().log("Register request received for username={}", req.getUsername());
        if(!req.getConfirmPassword().equals(req.getPassword())){
            return ResponseEntity.badRequest().body("Incorrect Password!");
        }
        UserResponse user = userService.register(req);
        return ResponseEntity.ok(user);
    }
    @PostMapping("/income")
    public  ResponseEntity<UserResponse> addIncome(@Valid @RequestBody AddMoneyRequest req){
        logger.atInfo().log("Add Income request received. ");
        Long userid = currentUserProvider.getCurrentUserId();
        UserResponse response = userService.addIncome(userid, req.getAmount());
        logger.atInfo().log("Income added. ");
        return ResponseEntity.ok(response);
    }
}
