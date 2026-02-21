package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Req.ReservedRequest;
import com.myApp.ExpenseTracker.Dto.ReservedResponseList;
import com.myApp.ExpenseTracker.Req.UpdateReserveRequest;
import com.myApp.ExpenseTracker.Model.Reserved;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Repository.ReservedRepository;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import com.myApp.ExpenseTracker.Utils.ResourceAlreadyExists;
import com.myApp.ExpenseTracker.Utils.ResourceNotFoundException;
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
        Optional<Reserved> optionalReserved = reservedRepo.findByUser_IdAndLabel(userid, req.getLabel().toLowerCase());
        if(optionalReserved.isPresent()){
            logger.atWarn().log("Duplicate Reserve fund creation attempt for user {}" , userid);
            return Status.ALREADY_EXISTS;
        }
        User user = userRepo.getReferenceById(userid);
        Reserved reserved = reservedRepo.save(new Reserved(user,req.getLabel().toLowerCase(),req.getNote(),req.getAmount()));
        logger.atInfo().log("Reserved created: {} for user {}" , reserved.getLabel(),userid);
        auditService.logSuccess(userid,EntityType.RESERVED, reserved.getId(), "Reserved amount created successful");
        return Status.CREATED;
    }
    @Transactional
    public Status deleteReserve( Long userid ,String label ){
        Optional<Reserved> optionalReserved = reservedRepo.findByUser_IdAndLabel(userid,label.toLowerCase());
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
    @Transactional(readOnly = true)
    public List<ReservedResponseList> listReserved(Long userid){
        List<Reserved> reservedList = reservedRepo.findByUser_Id(userid);
        List<ReservedResponseList> list = reservedList.stream()
                .map(r -> new ReservedResponseList(
                        r.getId(),
                        r.getLabel(),
                        r.getAmount(),
                        r.getUser().getUsername(),
                        r.getNote()
                ))
                .toList();
        if(!reservedList.isEmpty()){
            return list;
        }
        logger.atWarn().log("Reserved List don't exist for user {}" , userid);
        return new ArrayList<>();
    }
    //for update under transaction .save is optional ,
    // we are under persistence context so hibernate will handle it by dirty read
    @Transactional
    public Status updateReserveLabel(Long userid, UpdateReserveRequest req){
        Reserved res = reservedRepo
                .findByUser_IdAndLabel(userid, req.getOld_label().toLowerCase())
                .orElseThrow(() -> {
                    logger.atWarn().log("Update failed, no reserve found for user {}", userid);
                    auditService.logFailure(userid, EntityType.RESERVED, null, "Reserved fund not found");
                    return new ResourceNotFoundException("Reserve not found");
                });
        if(req.getNew_label() != null) {
            String newLabel = req.getNew_label().toLowerCase();
            reservedRepo.findByUser_IdAndLabel(userid, newLabel)
                    .ifPresent(r -> {
                        throw new ResourceAlreadyExists("Reserve label already exists");
                    });
            res.setLabel(newLabel);
        }
        if(req.getNote() != null) {
            res.setNote(req.getNote());
        }
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
        Reserved res = reservedRepo.findByUser_IdAndLabel(userid, label.toLowerCase())
                .orElseThrow(() ->{
                    logger.atWarn().log("Amount Update failed , no reserve found for user {}" , userid);
                    auditService.logFailure(userid, EntityType.RESERVED, null, "Reserved fund not found");
                    return new ResourceNotFoundException("Reserve not found");
                        });
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
        Reserved res = reservedRepo.findByUser_IdAndLabel(userid, label.toLowerCase())
                .orElseThrow(() ->{
                    logger.atWarn().log("Amount withdraw failed , no reserve found for user {}" , userid);
                    auditService.logFailure(userid, EntityType.RESERVED, null, "Reserved fund not found");
                    return new ResourceNotFoundException("Reserve not found");
                });
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
    }
    public BigDecimal getTotalReserved(Long userId) {
        return reservedRepo.sumReservedByUserId(userId);
    }
}
