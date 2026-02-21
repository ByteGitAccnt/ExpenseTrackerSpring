package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Dto.ExpenseResponseList;
import com.myApp.ExpenseTracker.Model.Category;
import com.myApp.ExpenseTracker.Model.Expense;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Req.AddExpenseReq;
import com.myApp.ExpenseTracker.Repository.ExpenseRepository;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import com.myApp.ExpenseTracker.Req.DateAndCatReq;
import com.myApp.ExpenseTracker.Req.DateReq;
import com.myApp.ExpenseTracker.Req.ExpenseUpdateReq;
import com.myApp.ExpenseTracker.Utils.ResourceNotFoundException;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepo;
    private final AuditService auditService;
    private final UserRepository userRepo;
    private final ReservedService reservedService;
    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);
    public ExpenseService(ExpenseRepository repo , AuditService audit,UserRepository userRepo,ReservedService reserve,CategoryService categoryService){
        this.expenseRepo = repo;
        this.auditService = audit;
        this.userRepo = userRepo;
        this.reservedService = reserve;
        this.categoryService = categoryService;
    }
    @Transactional
    public Status addExpense(Long userId, AddExpenseReq req) {
        try {
            if (Boolean.TRUE.equals(req.getIsReserved())
                    && (req.getLabel() == null || req.getLabel().isBlank())) {
                throw new BadRequestException("Label required when reserved is true");
            }
        } catch (BadRequestException e) {
            throw new RuntimeException(e);
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BigDecimal amount = req.getAmount();
        if (req.getIsReserved()) {
            Status reserveStatus = reservedService.withdrawAmount(
                    userId,
                    req.getLabel(),
                    amount
            );
            if (reserveStatus == Status.FAILED) {
                return Status.FAILED;
            }
            logger.atInfo().log("Amount:{} deducted from reserve for user {}" , req.getAmount(),userId);
        } else {
            BigDecimal available = user.getBalance().subtract(reservedService.getTotalReserved(userId));
            if (available.compareTo(amount) < 0) {
                logger.atWarn().log("Expense creation failed , Insufficient balance for user {}" , userId);
                return Status.FAILED;
            }
        }
        user.withdraw(amount);
        Category category = categoryService.findOrCreate(userId, req.getCategory());
        Expense expense = new Expense(
                amount,
                req.getExpenseDate(),
                req.getNote(),
                category,
                user
        );
        Expense exp = expenseRepo.save(expense);
        logger.atInfo().log("Expense created for user{}" , userId);
        auditService.logSuccess(userId,EntityType.EXPENSE, exp.getId(), "Expense created");
        return Status.CREATED;
    }
    @Transactional(readOnly = true)
    public List<ExpenseResponseList> listExpense(Long userid){
        List<Expense> expenseList = expenseRepo.findByUser_Id(userid);
        List<ExpenseResponseList> list = expenseList.stream()
                .map(r -> new ExpenseResponseList(
                        r.getId(),
                        r.getAmount(),
                        r.getExpenseDate(),
                        r.getNote(),
                        r.getCategory().getName()
                ))
                .toList();
        if (!expenseList.isEmpty()) return  list;
        logger.atWarn().log("Expense List don't exist for user {}" , userid);
        return new ArrayList<>();
    }
    @Transactional(readOnly = true)
    public List<ExpenseResponseList> listExpenseByDate(Long userid , DateReq req){
        List<Expense> expenseList = expenseRepo.findByUser_IdAndExpenseDateBetween(userid , req.getStartDate(),req.getEndDate());
        List<ExpenseResponseList> list = expenseList.stream()
                .map(r -> new ExpenseResponseList(
                        r.getId(),
                        r.getAmount(),
                        r.getExpenseDate(),
                        r.getNote(),
                        r.getCategory().getName()
                ))
                .toList();
        if (!expenseList.isEmpty()) return  list;
        logger.atWarn().log("Expense List don't exist for user {} with current date" , userid);
        return new ArrayList<>();
    }
    @Transactional(readOnly = true)
    public List<ExpenseResponseList> listExpenseByCategoryAndDate(Long userid , DateAndCatReq req){
        List<Expense> expenseList = expenseRepo.findByUser_IdAndCategory_IdAndExpenseDateBetween(
                userid ,
                req.getCatid(),
                req.getStartDate(),
                req.getEndDate()
        );
        List<ExpenseResponseList> list = expenseList.stream()
                .map(r -> new ExpenseResponseList(
                        r.getId(),
                        r.getAmount(),
                        r.getExpenseDate(),
                        r.getNote(),
                        r.getCategory().getName()
                ))
                .toList();
        if (!expenseList.isEmpty()) return  list;
        logger.atWarn().log("Expense List don't exist for user {} with current category and date" , userid);
        return new ArrayList<>();
    }
    @Transactional
    public Status deleteExpense(Long userid, Long expid) {
        Expense exp = expenseRepo.findByIdAndUser_Id(expid, userid)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        User user = userRepo.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setBalance(user.getBalance().add(exp.getAmount()));
        expenseRepo.delete(exp);
        auditService.logSuccess(userid, EntityType.EXPENSE, expid, "Expense deleted and balance restored");
        return Status.DELETED;
    }
    //for update under transaction .save is optional ,
    // we are under persistence context so hibernate will handle it by dirty read
    @Transactional
    public Status updateExpense(Long userid , ExpenseUpdateReq req){
        Expense expense = expenseRepo
                .findByIdAndUser_Id(req.getExp_id(), userid)
                .orElseThrow(() -> {
                    logger.atWarn().log("Updation failed ,Expense not found with the expid {} for user {}", req.getExp_id(), userid);
                    auditService.logFailure(userid,EntityType.EXPENSE,req.getExp_id(),"Not found");
                    return new ResourceNotFoundException("Expense not found");
                });
        if (req.getExp_date() != null) {
            expense.setExpenseDate(req.getExp_date());
        }
        if (req.getNote() != null && !req.getNote().isBlank()) {
            expense.setNote(req.getNote());
        }
        Category category = null;
        if (req.getCategory_name() != null && !req.getCategory_name().isBlank()) {
            category = categoryService.getByNameForUser(req.getCategory_name(),userid);
            expense.setCategory(category);
        }
        auditService.logUpdate(userid,EntityType.EXPENSE,req.getExp_id(),"Category,Note,Date",req.toString());
        logger.atInfo().log("Expense updated successfully for user {}" , userid);
        return Status.UPDATED;
    }
}

