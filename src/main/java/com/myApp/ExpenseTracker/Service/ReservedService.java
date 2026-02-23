package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Exeception.InsufficientBalanceException;
import com.myApp.ExpenseTracker.Req.ReservedRequest;
import com.myApp.ExpenseTracker.Dto.ReservedResponse;
import com.myApp.ExpenseTracker.Req.UpdateReserveRequest;
import com.myApp.ExpenseTracker.Model.Reserved;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Repository.ReservedRepository;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import com.myApp.ExpenseTracker.Exeception.ResourceAlreadyExists;
import com.myApp.ExpenseTracker.Exeception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


@Service
public class ReservedService {
    private final ReservedRepository reservedRepo;
    private final AuditService auditService;
    private final UserService userService;
    private final UserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(ReservedService.class);
    public ReservedService(ReservedRepository repo , AuditService audit,UserRepository userRepo , UserService service){
        this.reservedRepo = repo;
        this.auditService = audit;
        this.userRepo = userRepo;
        this.userService = service;
    }
    @Transactional
    public ReservedResponse addReserved( Long userid,ReservedRequest req){
        User user = userService.getUserByid(userid)
                .orElseThrow(() -> {
                    logger.atWarn().log("User doesn't exist for the userid : {}" , userid);
                    return new ResourceNotFoundException("user not found");
                });
        reservedRepo.findByUser_IdAndLabel(userid, req.getLabel().toLowerCase())
                .ifPresent( e -> {
                    logger.atWarn().log("The reserved fund already exist for the user ");
                    throw new ResourceAlreadyExists("Reserve fund already exist!");
                });
        Reserved reserved = reservedRepo.save(new Reserved(user,req.getLabel(),req.getNote(),req.getAmount()));
        logger.atInfo().log("Reserved created: {} for user {}" , reserved.getLabel(),userid);
        auditService.logSuccess(userid,EntityType.RESERVED, reserved.getId(), "Reserved amount created successful");
        return new ReservedResponse(reserved.getId(),reserved.getLabel(),reserved.getAmount(),reserved.getNote());
    }
    @Transactional
    public void deleteReserve( Long userid ,String label ){
        Reserved reserved = reservedRepo.findByUser_IdAndLabel(userid,label.toLowerCase())
                .orElseThrow(() -> {
                    logger.atWarn().log("Reserved don't exist for user : {} with label: {}" , userid , label);
                    auditService.logFailure(userid,EntityType.RESERVED,null ,"Reserved don't exist ");
                    return new ResourceNotFoundException("Reserve fund not found.");
                });
        reservedRepo.delete(reserved);
        logger.atInfo().log("Reserved amount deleted for user : {}" , userid);
        auditService.logSuccess(userid,EntityType.RESERVED,reserved.getId(),"Reserved amount deleted successful");
    }
    @Transactional(readOnly = true)
    public List<ReservedResponse> listReserved(Long userid) {
        return reservedRepo.findByUser_Id(userid)
                .stream()
                .map(r -> new ReservedResponse(
                        r.getId(),
                        r.getLabel(),
                        r.getAmount(),
                        r.getNote()
                ))
                .toList();
    }
    //for update under transaction .save is optional ,
    // we are under persistence context so hibernate will handle it by dirty read
    @Transactional
    public ReservedResponse updateReserveLabel(Long userid, UpdateReserveRequest req){
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
        return new ReservedResponse(res.getId(), res.getLabel(), res.getAmount(),res.getNote());
    }
    @Transactional
    public ReservedResponse addAmount( Long userid,String label , BigDecimal amnt){
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
            throw new InsufficientBalanceException("Insufficient balance in reserved fund");
        }
        res.setAmount(res.getAmount().add(amnt).setScale(2, RoundingMode.HALF_UP));

        auditService.logUpdate(userid, EntityType.RESERVED, res.getId(), "Amount", amnt.toString());
        return new ReservedResponse(res.getId(), res.getLabel(),res.getAmount(),res.getNote());
    }
    @Transactional
    public ReservedResponse withdrawAmount(Long userid,String label , BigDecimal amnt){
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
            throw new InsufficientBalanceException("Insufficient balance for reserved fund");
        }
        if (currentAmount.compareTo(amnt) < 0) {
            logger.atWarn().log("Deduction failed, insufficient reserved amount for user {}", userid);
            auditService.logFailure(userid, EntityType.RESERVED, res.getId(), "Insufficient reserved amount");
            throw new InsufficientBalanceException("Insufficient balance for reserved fund");
        }
        res.setAmount(res.getAmount().subtract(amnt).setScale(2, RoundingMode.HALF_UP));

        auditService.logUpdate(userid, EntityType.RESERVED, res.getId(), "Amount",amnt.toString());
        return new ReservedResponse(res.getId(), res.getLabel(),res.getAmount(),res.getNote());
    }
    public BigDecimal getTotalReserved(Long userId) {
        return reservedRepo.sumReservedByUserId(userId);
    }
}
