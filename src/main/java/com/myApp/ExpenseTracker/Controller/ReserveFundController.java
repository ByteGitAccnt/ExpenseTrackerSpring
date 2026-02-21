package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.*;
import com.myApp.ExpenseTracker.Req.DeletionRequest;
import com.myApp.ExpenseTracker.Req.ReservedMoneyRequest;
import com.myApp.ExpenseTracker.Req.ReservedRequest;
import com.myApp.ExpenseTracker.Req.UpdateReserveRequest;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.ReservedService;
import com.myApp.ExpenseTracker.Service.Status;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reserve")
public class ReserveFundController {
    private final ReservedService reservedService;
    private final CurrentUserProvider currentUserProvider;
    private static final Logger logger = LoggerFactory.getLogger(ReserveFundController.class);
    public ReserveFundController(ReservedService reservedService,CurrentUserProvider provider) {
        this.reservedService = reservedService;
        this.currentUserProvider = provider;
    }
    @PostMapping
    public ResponseEntity<?> addReserve(@Valid @RequestBody ReservedRequest req){
        logger.atInfo().log("Request for creating reserve fund received for user with label {}" , req.getLabel());
        Long userid = currentUserProvider.getCurrentUserId();
        Status success = reservedService.addReserved(userid, req);
        if(success == Status.CREATED){
            logger.atInfo().log("reserved fund created for label {} successful", req.getLabel());
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("reserve fund creation failed for label {}",req.getLabel());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Reserve with this label already exists");
    }
    @DeleteMapping
    public ResponseEntity<?> delete(@Valid @RequestBody DeletionRequest req){
        logger.atInfo().log("Request for deleting reserve fund received for label {}" ,req.getLabel());
        Long userid = currentUserProvider.getCurrentUserId();
        Status deleted = reservedService.deleteReserve(userid, req.getLabel());
        if(deleted == Status.DELETED){
            logger.atInfo().log("Reserve Fund deletion successful for label {}" ,req.getLabel());
            return ResponseEntity.noContent().build();
        }
        logger.atWarn().log("deletion failed , reserve fund not found! for label {}" , req.getLabel());
        return ResponseEntity.notFound().build();
    }
    @GetMapping
    public ResponseEntity<?> listReserve(){
        Long userid = currentUserProvider.getCurrentUserId();
        List<ReservedResponseList> reservedList = reservedService.listReserved(userid);
        if(!reservedList.isEmpty()){
            return ResponseEntity.ok(reservedList);
        }
        logger.atWarn().log("Reserve List not found!");
        return ResponseEntity.notFound().build();
    }
    @PutMapping
    public ResponseEntity<?> updateReserve(@Valid @RequestBody UpdateReserveRequest req){
        Long userid = currentUserProvider.getCurrentUserId();
        Status updated = reservedService.updateReserveLabel(userid , req);
        if(updated == Status.UPDATED){
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Failed to update the reserve for user {}" , userid);
        return ResponseEntity.notFound().build();
    }
    @PostMapping("/deposit")
    public ResponseEntity<?> addMoney(@Valid @RequestBody ReservedMoneyRequest req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request received for adding money for user {}" , userid);
        Status success = reservedService.addAmount(userid, req.getLabel(), req.getAmount());
        if (success == Status.UPDATED){
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Failed to update amount for reserve for user {}" , userid);
        return (success == Status.NOT_FOUND) ? ResponseEntity.notFound().build() : ResponseEntity.badRequest().build();
    }
    @PostMapping("/withdraw")
    public ResponseEntity<?> deductMoney(@Valid @RequestBody ReservedMoneyRequest req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request received for deduct money for user {}" , userid);
        Status success = reservedService.withdrawAmount(userid, req.getLabel(), req.getAmount());
        if (success == Status.UPDATED){
            return ResponseEntity.ok().build();
        }
        logger.atWarn().log("Failed to deduct amount for reserve for user {}" , userid);
        return (success == Status.NOT_FOUND) ? ResponseEntity.notFound().build() : ResponseEntity.badRequest().build();
    }

}

