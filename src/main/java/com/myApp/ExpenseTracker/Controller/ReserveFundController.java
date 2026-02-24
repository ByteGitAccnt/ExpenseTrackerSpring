package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.*;
import com.myApp.ExpenseTracker.Req.DeletionRequest;
import com.myApp.ExpenseTracker.Req.ReservedMoneyRequest;
import com.myApp.ExpenseTracker.Req.ReservedRequest;
import com.myApp.ExpenseTracker.Req.UpdateReserveRequest;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.ReservedService;
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
    public ResponseEntity<ReservedResponse> addReserve(@Valid @RequestBody ReservedRequest req){
        logger.atInfo().log("Request for creating reserve fund received for user with label {}" , req.getLabel());
        Long userid = currentUserProvider.getCurrentUserId();
        ReservedResponse response = reservedService.addReserved(userid, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @DeleteMapping
    public ResponseEntity<?> delete(@Valid @RequestBody DeletionRequest req){
        logger.atInfo().log("Request for deleting reserve fund received for label {}" ,req.getLabel());
        Long userid = currentUserProvider.getCurrentUserId();
        reservedService.deleteReserve(userid, req.getLabel());
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<List<ReservedResponse>> listReserved() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<ReservedResponse> response = reservedService.listReserved(userId);
        return ResponseEntity.ok(response);
    }
    @PutMapping
    public ResponseEntity<ReservedResponse> updateReserve(@Valid @RequestBody UpdateReserveRequest req){
        Long userid = currentUserProvider.getCurrentUserId();
        ReservedResponse response = reservedService.updateReserveLabel(userid , req);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/deposit")
    public ResponseEntity<ReservedResponse> addMoney(@Valid @RequestBody ReservedMoneyRequest req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request received for adding money for user {}" , userid);
        ReservedResponse response = reservedService.addAmount(userid, req.getLabel(), req.getAmount());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/withdraw")
    public ResponseEntity<?> deductMoney(@Valid @RequestBody ReservedMoneyRequest req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request received for deduct money for user {}" , userid);
        ReservedResponse response = reservedService.withdrawAmount(userid, req.getLabel(), req.getAmount());
        return ResponseEntity.ok(response);
    }

}

