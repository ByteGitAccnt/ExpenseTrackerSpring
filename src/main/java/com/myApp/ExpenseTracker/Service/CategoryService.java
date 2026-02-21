package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Dto.CategoryResponseList;
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
    public CategoryService(CategoryRepository catRepo,AuditService auditService,UserRepository userRepo ){
        this.auditService = auditService;
        this.catRepo = catRepo;
        this.userRepo = userRepo;
    }
    public Category addCategory(Long userid,String catname){
        User user = userRepo.getReferenceById(userid);
        Category  cat = catRepo.save(new Category(catname,user));
        logger.atInfo().log("Category created: {} for user: {}" ,catname , userid);
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
    public Status updateCategory( Long userId,String oldname , String newname){
        if(oldname.equalsIgnoreCase(newname)){
            logger.atWarn().log("Category updation failed , old name and new name are same for user {}" , userId);
            return Status.FAILED;
        }
        if(catRepo.existsByNameAndUser_Id(newname.toLowerCase(), userId)){
            logger.atWarn().log("Category updation failed , new name already exists for user {}" , userId);
            return Status.ALREADY_EXISTS;
        }
        Optional<Category> opCat = catRepo.findByNameAndUser_Id(oldname.toLowerCase(), userId);
        boolean[] success = new boolean[1];
        opCat.ifPresentOrElse(cat -> {
            cat.setName(newname);
            auditService.logUpdate(userId,EntityType.CATEGORY,cat.getId(),"NAME" , newname );
            success[0] = true;
        }, () -> {
            auditService.logFailure(userId,EntityType.CATEGORY,null,"Entity not found");
            success[0] = false;
        });
        if(success[0]){
            logger.atInfo().log("Category Updated successful for user: {} from {} to {}",userId,oldname,newname);
            return Status.UPDATED;
        }
        logger.atWarn().log("Category updation failed , for user {}, Not found category {}" , userId,oldname);
        return Status.FAILED;
    }
    @Transactional
    public Status deleteCategory(Long userid, String name){
        Optional<Category> opCat = catRepo.findByNameAndUser_Id(name.toLowerCase() , userid);
        if(opCat.isPresent()){
         Category cat = opCat.get();
         catRepo.delete(cat);
         auditService.logSuccess(userid,EntityType.CATEGORY, cat.getId(), "Category: " + cat.getName() + "deleted successfully");
         logger.atInfo().log("Category {} deleted successful for user {}" , name , userid);
         return Status.SUCCESS;
        }
        auditService.logFailure(userid,EntityType.CATEGORY,null, "Category not found");
        logger.atWarn().log("Category {} deleted failed for user {}" , name , userid);
        return Status.FAILED;
    }
    @Transactional(readOnly = true)
    public List<CategoryResponseList> listCategory(Long userid){
        List<Category> categoryList = catRepo.findByUser_Id(userid);
        List<CategoryResponseList> list = categoryList.stream()
                .map(r -> new CategoryResponseList(
                        r.getId(),
                        r.getName(),
                        r.getUser().getUsername()
                ))
                .toList();
        if (!categoryList.isEmpty())return list;
        logger.atWarn().log("Category List don't exist for user {}" , userid);
        return new ArrayList<>();
    }
    public Category getByNameForUser(String name, Long userId) {
        return findOrCreate(userId,name.toLowerCase());
    }

}

