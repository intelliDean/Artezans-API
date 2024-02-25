package com.api.artezans.category.controller;

import com.api.artezans.category.data.dtos.CategoryNameDto;
import com.api.artezans.category.data.dtos.CategoryRequest;
import com.api.artezans.category.data.model.Category;
import com.api.artezans.category.service.interfaces.CategoryNameService;
import com.api.artezans.category.service.interfaces.CategoryService;
import com.api.artezans.utils.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class ServiceCategoryController {
    private final CategoryNameService categoryNameService;
    private final CategoryService categoryService;

    public ApiResponse addCategoryName(List<CategoryNameDto> categoryNames) {
        return categoryNameService.addCategoryName(categoryNames);
    }

    public List<String> getCategoryNames() {
        return categoryNameService.findAllCategoryNames();
    }

    public ApiResponse addServiceCategory(CategoryRequest request) {
        return categoryService.addServiceCategory(request);
    }

    public List<String> viewServicesByCategoryName(String categoryName) {
        return categoryService.viewServicesByCategoryName(categoryName);
    }

    public ApiResponse deleteCategoryName(CategoryNameDto categoryName) {
        return categoryNameService.deleteCategoryName(categoryName);
    }

    public List<Category> viewAllCategories() {
        return categoryService.viewAllCategories();
    }
}
