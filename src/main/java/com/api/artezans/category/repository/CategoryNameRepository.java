package com.api.artezans.category.repository;

import com.api.artezans.category.data.model.CategoryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CategoryNameRepository extends JpaRepository<CategoryName, Long> {
    boolean existsByNameIgnoreCase(String categoryName);
    Optional<CategoryName> findByNameIgnoreCase (String categoryName);
}
