package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.ExpenseResponseList;
import com.myApp.ExpenseTracker.Req.*;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.ExpenseService;
import com.myApp.ExpenseTracker.Service.Status;
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
    public ResponseEntity<?> addExpense(@Valid @RequestBody AddExpenseReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for creating expense received for user {}" ,userid);
        Status created = expenseService.addExpense(userid,req);
        if(created == Status.CREATED){
            logger.atInfo().log("Expense created for user {}"  , userid);
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Expense creation failed for user {}" , userid);
        return ResponseEntity.badRequest().build();
    }
    @GetMapping
    public ResponseEntity<?> list(){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense listing received for user {}" , userid);
        List<ExpenseResponseList> list = expenseService.listExpense(userid);
        if (list.isEmpty()) {
            logger.atWarn().log("expense list not found for user {}" , userid);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }
    @GetMapping("/date")
    public ResponseEntity<?> list(@Valid @RequestBody DateReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense listing by date received for user {}" , userid);
        List<ExpenseResponseList> list = expenseService.listExpenseByDate(userid ,req );
        if (list.isEmpty()) {
            logger.atWarn().log("expense  expense listing by date not found for user {}" , userid);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }
    @GetMapping("/category")
    public ResponseEntity<?> list(@Valid @RequestBody DateAndCatReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense listing by date and category received for user {}" , userid);
        List<ExpenseResponseList> list = expenseService.listExpenseByCategoryAndDate(userid ,req );
        if (list.isEmpty()) {
            logger.atWarn().log("expense  expense listing by date and category not found for user {}" , userid);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }
    @DeleteMapping("/{exp_id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long exp_id){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense deletion received for user {}" , userid);
        Status deleted = expenseService.deleteExpense(userid,exp_id);
        if (deleted == Status.DELETED){
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Expense deletion failed for user {}" , userid);
        return ResponseEntity.notFound().build();
    }
    @PatchMapping
    public ResponseEntity<?> updateExpense(@Valid @RequestBody ExpenseUpdateReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for expense updation received for user {}", userid);
        Status updated = expenseService.updateExpense(userid,req);
        if(updated == Status.UPDATED){
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Expense updation failed for user {}" , userid);
        return ResponseEntity.notFound().build();
    }
}
