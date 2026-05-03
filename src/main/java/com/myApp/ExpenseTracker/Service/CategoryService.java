package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Dto.CategoryResponse;
import com.myApp.ExpenseTracker.Dto.CategoryResponseList;
import com.myApp.ExpenseTracker.Exeception.DuplicateCategoryNameException;
import com.myApp.ExpenseTracker.Exeception.ResourceAlreadyExists;
import com.myApp.ExpenseTracker.Exeception.ResourceNotFoundException;
import com.myApp.ExpenseTracker.Model.Category;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Repository.CategoryRepository;
import com.myApp.ExpenseTracker.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class CategoryService {
    private final AuditService auditService;
    private final UserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository catRepo;
    private List<Category> categoryList;
    public CategoryService(CategoryRepository catRepo,AuditService auditService,UserRepository userRepo ){
        this.auditService = auditService;
        this.catRepo = catRepo;
        this.userRepo = userRepo;
        this.categoryList = new ArrayList<>();
    }
    public Category addCategory(Long userid,String catname){
        User user = userRepo.getReferenceById(userid);
        Category  cat = catRepo.save(new Category(catname,user));
        auditService.logSuccess(userid,EntityType.CATEGORY,cat.getId(),"Category created: " + cat.getName());
        return cat;
    }
    @Transactional
    public Category findOrCreate(Long userId,String catName) {
        if (catRepo.existsByNameAndUser_Id(catName.toLowerCase(), userId)) {
            Optional<Category> opCat = catRepo.findByNameAndUser_Id(catName.toLowerCase(), userId);
            return opCat.orElse(null);
        }else{
            return addCategory(userId, catName);
        }
    }
    //for update under transaction .save is optional ,
    // we are under persistence context so hibernate will handle it by dirty read
    @Transactional
    public CategoryResponse updateCategory( Long userId,String oldname , String newname){
        if(oldname.equalsIgnoreCase(newname)){
            throw new DuplicateCategoryNameException("old name and new name are same! for user : " + userId );
        }
        if(catRepo.existsByNameAndUser_Id(newname.toLowerCase(), userId)){
            throw new ResourceAlreadyExists("category name already exists! for user:" + userId);
        }
        Category cat = catRepo.findByNameAndUser_Id(oldname.toLowerCase(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for the name : " + oldname + " for the user : " + userId));
        cat.setName(newname);
        auditService.logUpdate(userId,EntityType.CATEGORY,cat.getId(),"NAME" , newname );
        logger.atInfo().log("Category Updated successful for user: {} from {} to {}",userId,oldname,newname);
       return new CategoryResponse(cat.getId() , cat.getName());
    }
    @Transactional
    public void deleteCategory(Long userid, String name){
         Category cat = catRepo.findByNameAndUser_Id(name.toLowerCase() , userid)
                 .orElseThrow(() -> new ResourceNotFoundException("Category not found for the name : " + name + " for the user : " + userid));
         catRepo.delete(cat);
         auditService.logSuccess(userid,EntityType.CATEGORY, cat.getId(), "Category: " + cat.getName() + "deleted successfully");
         logger.atInfo().log("Category {} deleted successful for user {}" , name , userid);
    }
    @Transactional(readOnly = true)
    public List<CategoryResponseList> listCategory(Long userid){
        cache(userid);
        List<CategoryResponseList> list = categoryList.stream()
                .map(r -> new CategoryResponseList(
                        r.getId(),
                        r.getName(),
                        r.getUser().getUsername()
                ))
                .toList();
        if (!categoryList.isEmpty())return list;
        logger.atInfo().log("Category List don't exist for user {}" , userid);
        return new ArrayList<>();
    }
    public Category getByNameForUser(String name, Long userId) {
        return findOrCreate(userId,name.toLowerCase());
    }
    @Transactional(readOnly = true)
    private void  cache(Long userId){
        if(categoryList.isEmpty()) categoryList = catRepo.findByUser_Id(userId);
    }
}

