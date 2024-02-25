package com.api.artezans.category.service.interfaces;



import com.api.artezans.category.data.dtos.CategoryNameDto;
import com.api.artezans.utils.ApiResponse;

import java.util.List;

public interface CategoryNameService {

    ApiResponse addCategoryName(List<CategoryNameDto> categoryNames);
    List<String> findAllCategoryNames();
    ApiResponse deleteCategoryName(CategoryNameDto categoryName);

}
