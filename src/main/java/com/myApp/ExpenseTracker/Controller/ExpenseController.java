package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.ExpenseResponse;
import com.myApp.ExpenseTracker.Req.*;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.ExpenseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expense")
public class ExpenseController {
    private final ExpenseService expenseService;
    private final CurrentUserProvider currentUserProvider;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
    public ExpenseController(ExpenseService service,CurrentUserProvider provider) {
        this.expenseService = service;
        this.currentUserProvider = provider;
    }
    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(@Valid @RequestBody AddExpenseReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for creating expense received for user {}" ,userid);
        ExpenseResponse response = expenseService.addExpense(userid,req);
        logger.atInfo().log("Expense created for user {}"  , userid);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense listing received for user {}" , userid);
        List<ExpenseResponse> list = expenseService.listExpense(userid);
        return ResponseEntity.ok(list);
    }
    @GetMapping("/date")
    public ResponseEntity<List<ExpenseResponse>> list(@Valid @RequestBody DateReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense listing by date received for user {}" , userid);
        List<ExpenseResponse> list = expenseService.listExpenseByDate(userid ,req );
        return ResponseEntity.ok(list);
    }
    @GetMapping("/category")
    public ResponseEntity<List<ExpenseResponse>> list(@Valid @RequestBody DateAndCatReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense listing by date and category received for user {}" , userid);
        List<ExpenseResponse> list = expenseService.listExpenseByCategoryAndDate(userid ,req );
        return ResponseEntity.ok(list);
    }
    @DeleteMapping("/{exp_id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long exp_id){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense deletion received for user {}" , userid);
        expenseService.deleteExpense(userid,exp_id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping
    public ResponseEntity<ExpenseResponse> updateExpense(@Valid @RequestBody ExpenseUpdateReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense updation received for user {}", userid);
        ExpenseResponse response = expenseService.updateExpense(userid,req);
        return ResponseEntity.ok(response);
    }
}
