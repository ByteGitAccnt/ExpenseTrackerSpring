package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Dto.ExpenseResponse;
import com.myApp.ExpenseTracker.Exeception.RequiredException;
import com.myApp.ExpenseTracker.Model.Category;
import com.myApp.ExpenseTracker.Model.Expense;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Req.AddExpenseReq;
import com.myApp.ExpenseTracker.Repository.ExpenseRepository;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import com.myApp.ExpenseTracker.Req.DateAndCatReq;
import com.myApp.ExpenseTracker.Req.DateReq;
import com.myApp.ExpenseTracker.Req.ExpenseUpdateReq;
import com.myApp.ExpenseTracker.Exeception.InsufficientBalanceException;
import com.myApp.ExpenseTracker.Exeception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
    public ExpenseResponse addExpense(Long userId, AddExpenseReq req) {
        if (Boolean.TRUE.equals(req.getIsReserved())
                && (req.getLabel() == null || req.getLabel().isBlank())) {
            throw new RequiredException("Label required when reserved is true");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with userid:" + userId));
        BigDecimal amount = req.getAmount();
        if (req.getIsReserved()) {
            reservedService.withdrawAmount(
                    userId,
                    req.getLabel(),
                    amount
            );
            logger.atInfo().log("Amount:{} deducted from reserve for user {}" , req.getAmount(),userId);
        } else {
            BigDecimal available = user.getBalance().subtract(reservedService.getTotalReserved(userId));
            if (available.compareTo(amount) < 0) {
                throw  new InsufficientBalanceException("Insufficient balance! for user {}"+ userId);
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
        auditService.logSuccess(userId,EntityType.EXPENSE, exp.getId(), "Expense created");
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getNote(),
                expense.getCategory().getName());
    }
    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpense(Long userid){
        List<Expense> expenseList = expenseRepo.findByUser_Id(userid);
        return expenseList.stream()
                .map(e -> new ExpenseResponse(
                        e.getId(),
                        e.getAmount(),
                        e.getExpenseDate(),
                        e.getNote(),
                        e.getCategory().getName()
                ))
                .toList();
    }
    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpenseByDate(Long userid , DateReq req){
        List<Expense> expenseList = expenseRepo.findByUser_IdAndExpenseDateBetween(userid , req.getStartDate(),req.getEndDate());
        return expenseList.stream()
                .map(e -> new ExpenseResponse(
                        e.getId(),
                        e.getAmount(),
                        e.getExpenseDate(),
                        e.getNote(),
                        e.getCategory().getName()
                ))
                .toList();
    }
    @Transactional(readOnly = true)
    public List<ExpenseResponse> listExpenseByCategoryAndDate(Long userid , DateAndCatReq req){
        List<Expense> expenseList = expenseRepo.findByUser_IdAndCategory_IdAndExpenseDateBetween(
                userid ,
                req.getCatid(),
                req.getStartDate(),
                req.getEndDate()
        );
        return expenseList.stream()
                .map(r -> new ExpenseResponse(
                        r.getId(),
                        r.getAmount(),
                        r.getExpenseDate(),
                        r.getNote(),
                        r.getCategory().getName()
                ))
                .toList();
    }
    @Transactional
    public void deleteExpense(Long userid, Long expid) {
        Expense exp = expenseRepo.findByIdAndUser_Id(expid, userid)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found for user:" + userid ));
        User user = userRepo.findByIdForUpdate(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        user.setBalance(user.getBalance().add(exp.getAmount()));
        expenseRepo.delete(exp);
        auditService.logSuccess(userid, EntityType.EXPENSE, expid, "Expense deleted and balance restored");
    }
    //for update under transaction .save is optional ,
    // we are under persistence context so hibernate will handle it by dirty read
    @Transactional
    public ExpenseResponse updateExpense(Long userid , ExpenseUpdateReq req){
        Expense expense = expenseRepo
                .findByIdAndUser_Id(req.getExp_id(), userid)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found for user:" + userid));
        if (req.getExp_date() != null) {
            expense.setExpenseDate(req.getExp_date());
        }
        if (req.getNote() != null && !req.getNote().isBlank()) {
            expense.setNote(req.getNote());
        }
        Category category ;
        if (req.getCategory_name() != null && !req.getCategory_name().isBlank()) {
            category = categoryService.getByNameForUser(req.getCategory_name(),userid);
            expense.setCategory(category);
        }
        auditService.logUpdate(userid,EntityType.EXPENSE,req.getExp_id(),"Category,Note,Date",req.toString());
        logger.atInfo().log("Expense updated successfully for user {}" , userid);
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getNote(),
                expense.getCategory().getName()
        );
    }
}

