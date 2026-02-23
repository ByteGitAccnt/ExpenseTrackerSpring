package com.myApp.ExpenseTracker.Controller;

import com.myApp.ExpenseTracker.Dto.CategoryResponse;
import com.myApp.ExpenseTracker.Req.CategoryDeletionReq;
import com.myApp.ExpenseTracker.Dto.CategoryResponseList;
import com.myApp.ExpenseTracker.Req.CategoryUpdateRequest;
import com.myApp.ExpenseTracker.Service.CategoryService;
import com.myApp.ExpenseTracker.Service.CurrentUserProvider;
import com.myApp.ExpenseTracker.Service.Status;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;
    private final CurrentUserProvider currentUserProvider;
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    public CategoryController(CategoryService categoryService, CurrentUserProvider provider) {
        this.categoryService = categoryService;
        this.currentUserProvider = provider;
    }
    @PatchMapping
    public ResponseEntity<?> updateCategory(@Valid @RequestBody CategoryUpdateRequest req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for category: {}  update received for user {}" ,req.getOld_name(), userid);
        CategoryResponse response = categoryService.updateCategory(userid,req.getOld_name(), req.getNew_name());
        return ResponseEntity.ok(response);
    }
    @DeleteMapping
    public ResponseEntity<?> deleteCategory(@Valid @RequestBody CategoryDeletionReq req){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for category: {}  delete received for user {}" ,req.getName(), userid);
        categoryService.deleteCategory(userid, req.getName());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/list")
    public ResponseEntity<?> listCategory(){
        Long userid = currentUserProvider.getCurrentUserId();
        logger.atInfo().log("Request for listing category received for user {}" ,userid);
        List<CategoryResponseList> categoryList = categoryService.listCategory(userid);
        if (!categoryList.isEmpty()) return ResponseEntity.ok(categoryList);
        logger.atWarn().log("Category list not found for user {}" , userid);
        return ResponseEntity.notFound().build();
    }
}
