package com.api.artezans.category.service.interfaces;



import com.api.artezans.category.data.dtos.CategoryRequest;
import com.api.artezans.category.data.model.Category;
import com.api.artezans.utils.ApiResponse;

import java.util.List;

public interface CategoryService {

    ApiResponse addServiceCategory(CategoryRequest request);

    Category findByCategoryName(String categoryName);
    List<String> viewServicesByCategoryName(String categoryName);
    List<Category> viewAllCategories();
}
