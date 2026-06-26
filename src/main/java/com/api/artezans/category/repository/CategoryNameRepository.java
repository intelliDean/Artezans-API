package com.api.artezans.category.repository;

import com.api.artezans.category.data.model.CategoryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryNameRepository extends JpaRepository<CategoryName, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<CategoryName> findByNameIgnoreCase(String name);

    @Query("SELECT c.name FROM CategoryName c WHERE UPPER(c.name) IN :names")
    Set<String> findExistingNames(@Param("names") Set<String> names);


    @Query("SELECT c.name FROM CategoryName c")
    List<String> findAllNames();
}