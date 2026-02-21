package com.myApp.ExpenseTracker.Repository;

import com.myApp.ExpenseTracker.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByNameAndUser_Id(String name, Long userId);
    boolean existsByNameAndUser_Id(String name, Long userId);
    List<Category> findByUser_Id(Long userId);
}
