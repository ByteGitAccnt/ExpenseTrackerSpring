package com.myApp.ExpenseTracker.Controller;


import com.myApp.ExpenseTracker.Dto.AccntBalance;
import com.myApp.ExpenseTracker.Dto.AuthResponse;
import com.myApp.ExpenseTracker.Dto.RefreshResponse;
import com.myApp.ExpenseTracker.Dto.UserResponse;
import com.myApp.ExpenseTracker.Model.CustomUserDetails;
import com.myApp.ExpenseTracker.Model.RefreshToken;
import com.myApp.ExpenseTracker.Req.AddMoneyRequest;
import com.myApp.ExpenseTracker.Req.LoginRequest;
import com.myApp.ExpenseTracker.Req.RegisterRequest;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.JwtService;
import com.myApp.ExpenseTracker.Service.RefreshTokenService;
import com.myApp.ExpenseTracker.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    public AuthController(UserService userService,CurrentUserProvider provider,AuthenticationManager authenticationManager,
                          JwtService jwtService,RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.currentUserProvider = provider;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.atInfo().log("Login request received for username={}", request.getUsername());
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtService.generateToken(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userid = currentUserProvider.getCurrentUserId();
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                      userDetails.getUser()
                        );
        return ResponseEntity.ok(new AuthResponse(
                token,
                refreshToken.getToken(),
                userid,
                Instant.now().plusSeconds(3600)
        ));
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
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(HttpServletRequest req){
        logger.atInfo().log("refresh token request received");
        String refreshTokenValue = req.getHeader("Refresh-Token");
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
        String token;
        try {
            token = jwtService.refresh(refreshToken.getUser());
        } catch (Exception e) {
            logger.atError().log("JWT refresh failed", e);
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(new RefreshResponse(token,refreshToken.getToken()));
    }
    @PostMapping("/income")
    public  ResponseEntity<UserResponse> addIncome(@Valid @RequestBody AddMoneyRequest req){
        logger.atInfo().log("Add Income request received. ");
        Long userid = currentUserProvider.getCurrentUserId();
        UserResponse response = userService.addIncome(userid, req.getAmount());
        logger.atInfo().log("Income added. ");
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        logger.atInfo().log("Logout request received");
        Long userId = currentUserProvider.getCurrentUserId();
        refreshTokenService.deleteByUserId(userId);
        logger.atInfo().log("Logout successful");
        return ResponseEntity.ok("Logged out successfully");
    }
    @GetMapping("/balance")
    public ResponseEntity<?> balance(){
        Long userid = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new AccntBalance(userService.getUserByid(userid).getBalance()));
    }
}
