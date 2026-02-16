package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Dto.ReservedRequest;
import com.myApp.ExpenseTracker.Dto.UpdateReserveRequest;
import com.myApp.ExpenseTracker.Model.Reserved;
import com.myApp.ExpenseTracker.Repository.ReservedRepository;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservedService {
    private final ReservedRepository reservedRepo;
    private final AuditService auditService;
    private final UserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(ReservedService.class);
    public ReservedService(ReservedRepository repo , AuditService audit,UserRepository userRepo){
        this.reservedRepo = repo;
        this.auditService = audit;
        this.userRepo = userRepo;
    }
    @Transactional
    public Status addReserved( Long userid,ReservedRequest req){
        Optional<Reserved> optionalReserved = reservedRepo.findByUserIdAndLabel(userid, req.getLabel().toLowerCase());
        if(optionalReserved.isPresent()){
            logger.atWarn().log("Duplicate Reserve fund creation attempt for user {}" , userid);
            return Status.ALREADY_EXISTS;
        }
        Reserved reserved = reservedRepo.save(new Reserved(userid,req.getLabel(),req.getNote(),req.getAmount()));
        logger.atInfo().log("Reserved created: {} for user {}" , reserved.getLabel(),userid);
        auditService.logSuccess(userid,EntityType.RESERVED, reserved.getId(), "Reserved amount created successful");
        return Status.CREATED;
    }
    @Transactional
    public Status deleteReserve( Long userid ,String label ){
        Optional<Reserved> optionalReserved = reservedRepo.findByUserIdAndLabel(userid,label.toLowerCase());
        if(optionalReserved.isPresent()){
            Reserved reserved = optionalReserved.get();
            reservedRepo.delete(reserved);
            logger.atInfo().log("Reserved amount deleted for user : {}" , userid);
            auditService.logSuccess(userid,EntityType.RESERVED,reserved.getId(),"Reserved amount deleted successful");
            return Status.DELETED;
        }
        auditService.logFailure(userid,EntityType.RESERVED,null ,"Reserved don't exist ");
        logger.atWarn().log("Reserved don't exist for user : {} with label: {}" , userid , label);
        return Status.NOT_FOUND;
    }
    public List<Reserved> listReserved(Long userid){
        List<Reserved> reservedList = reservedRepo.findByUserId(userid);
        if(!reservedList.isEmpty()){
            return reservedList;
        }
        logger.atWarn().log("Reserved List don't exist for user {}" , userid);
        return new ArrayList<>();
    }
    @Transactional
    public Status updateReserveLabel(Long userid,UpdateReserveRequest req){
        Optional<Reserved> reserved = reservedRepo.findByUserIdAndLabel(userid, req.getOld_label().toLowerCase());
        Reserved res ;
        if(reserved.isEmpty()){
            logger.atWarn().log("Update failed , no reserve found for user {}" , userid);
            auditService.logFailure(userid, EntityType.RESERVED, null, "Reserved fund not found");
            return Status.NOT_FOUND;
        }
        res = reserved.get();
        if(req.getNew_label() != null) res.setLabel(req.getNew_label());
        if(req.getNote() != null) res.setNote(req.getNote());
        reservedRepo.save(res);
        auditService.logUpdate(userid, EntityType.RESERVED, res.getId(), "Reserve updated", req.toString());
        return Status.UPDATED;
    }
    @Transactional
    public Status addAmount( Long userid,String label , BigDecimal amnt){
        if (amnt == null || amnt.compareTo(BigDecimal.ZERO) <= 0) {
            logger.atWarn().log("Amount Update failed , invalid amount for user {}", userid);
            auditService.logFailure(userid, EntityType.RESERVED, null, "Invalid amount");
            return Status.FAILED;
        }
        Optional<Reserved> reservedOpt = reservedRepo.findByUserIdAndLabel(userid, label.toLowerCase());
        if(reservedOpt.isEmpty() ){
            logger.atWarn().log("Amount Update failed , no reserve found for user {}" , userid);
            auditService.logFailure(userid, EntityType.RESERVED, null, "Reserved fund not found");
            return Status.NOT_FOUND;
        }
        Reserved res = reservedOpt.get();

        BigDecimal balance = userRepo.findBalanceById(userid);
        if (balance == null) balance = BigDecimal.ZERO;

        BigDecimal totalReserved = reservedRepo.sumReservedByUserId(userid);
        if (totalReserved == null) totalReserved = BigDecimal.ZERO;
        BigDecimal available = balance.subtract(totalReserved);

        if (available.compareTo(amnt) < 0) {
            logger.atWarn().log("Amount Update failed , insufficient available balance for user {}", userid);
            auditService.logFailure(userid, EntityType.RESERVED, res.getId(), "Insufficient balance");
            return Status.FAILED;
        }
        res.setAmount(res.getAmount().add(amnt).setScale(2, RoundingMode.HALF_UP));
        reservedRepo.save(res);
        auditService.logUpdate(userid, EntityType.RESERVED, res.getId(), "Amount", amnt.toString());
        return Status.UPDATED;
    }
    @Transactional
    public Status withdrawAmount(Long userid,String label , BigDecimal amnt){
        if (amnt == null || amnt.compareTo(BigDecimal.ZERO) <= 0) {
            logger.atWarn().log("Amount deduction failed , invalid amount for user {}", userid);
            auditService.logFailure(userid, EntityType.RESERVED, null, "Invalid amount");
            return Status.FAILED;
        }
        Optional<Reserved> reservedOpt = reservedRepo.findByUserIdAndLabel(userid, label.toLowerCase());
        if(reservedOpt.isEmpty() ){
            logger.atWarn().log("Amount deduction failed , no reserve found for user {}" , userid);
            auditService.logFailure(userid, EntityType.RESERVED, null, "Reserved fund not found");
            return Status.NOT_FOUND;
        }
        Reserved res = reservedOpt.get();
        BigDecimal currentAmount = res.getAmount();
        if (currentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.atWarn().log("Deduction failed, reserve empty for user {}", userid);
            auditService.logFailure(userid, EntityType.RESERVED, res.getId(), "Reserve is empty");
            return Status.FAILED;
        }
        if (currentAmount.compareTo(amnt) < 0) {
            logger.atWarn().log("Deduction failed, insufficient reserved amount for user {}", userid);
            auditService.logFailure(userid, EntityType.RESERVED, res.getId(), "Insufficient reserved amount");
            return Status.FAILED;
        }
        res.setAmount(res.getAmount().subtract(amnt));
        reservedRepo.save(res);
        auditService.logUpdate(userid, EntityType.RESERVED, res.getId(), "Amount",amnt.toString());
        return Status.UPDATED;
        // need to deduct balance , but decide while expense adding form there or from here
    }
}
