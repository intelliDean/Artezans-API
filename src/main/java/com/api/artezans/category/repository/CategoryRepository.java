package com.api.artezans.category.repository;

import com.api.artezans.category.data.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findCategoryByCategoryNameIgnoreCase(String serviceCategory);

    @Query("SELECT c.categoryName FROM Category c")
    List<String> findAllCategoryNames();
}
