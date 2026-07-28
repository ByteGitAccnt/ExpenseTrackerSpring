package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.AccntBalance;
import com.myApp.ExpenseTracker.Dto.UserResponse;
import com.myApp.ExpenseTracker.Req.AddMoneyRequest;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/Accnt")
public class BalanceController {
    private final CurrentUserProvider currentUserProvider;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    public BalanceController(CurrentUserProvider userProvider, UserService userService) {
        this.currentUserProvider = userProvider;
        this.userService = userService;
    }
    @GetMapping("/balance")
    public ResponseEntity<?> balance(){
        logger.atInfo().log("Checking balance. ");
        Long userid = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new AccntBalance(userService.getUserByid(userid).getBalance()));
    }
    @PostMapping("/income")
    public  ResponseEntity<UserResponse> addIncome(@Valid @RequestBody AddMoneyRequest req){
        logger.atInfo().log("Add Income request received. ");
        Long userid = currentUserProvider.getCurrentUserId();
        UserResponse response = userService.addIncome(userid, req.getAmount() , req.getDescription());
        logger.atInfo().log("Income added. ");
        return ResponseEntity.ok(response);
    }
}
