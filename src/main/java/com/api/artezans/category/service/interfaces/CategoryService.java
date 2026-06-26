package com.api.artezans.category.service.interfaces;


import com.api.artezans.category.data.dtos.CategoryDTO;
import com.api.artezans.category.data.dtos.CategoryRequest;
import com.api.artezans.category.data.model.Category;
import com.api.artezans.utils.ApiResponse;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    ApiResponse addServiceCategory(CategoryRequest request);

    Optional<Category> findByCategoryName(String categoryName); // changed to Optional

    List<String> viewServicesByCategoryName(String categoryName);

    List<CategoryDTO> viewAllCategories();
}