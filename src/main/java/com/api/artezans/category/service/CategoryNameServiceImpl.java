package com.api.artezans.category.service;


import com.api.artezans.category.data.dtos.CategoryNameDto;
import com.api.artezans.category.data.model.CategoryName;
import com.api.artezans.category.repository.CategoryNameRepository;
import com.api.artezans.category.service.interfaces.CategoryNameService;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.utils.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.api.artezans.utils.ApiResponse.apiResponse;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryNameServiceImpl implements CategoryNameService {

    private final CategoryNameRepository categoryNameRepository;


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse addCategoryName(List<CategoryNameDto> categoryNameDtos) {
        List<CategoryName> names = categoryNameDtos.stream()
                .filter(categoryNameDto ->
                        !categoryNameRepository.existsByNameIgnoreCase(categoryNameDto.getCategoryName()))
                .map(categoryNameDto -> CategoryName.builder()
                        .name(categoryNameDto.getCategoryName().toUpperCase())
                        .build())
                .toList();
        if (!names.isEmpty()) {
            categoryNameRepository.saveAll(names);
            return apiResponse("Category name saved successfully");
        }
        throw new TaskHubException("Already existed");
    }

    @Override
    public List<String> findAllCategoryNames() {
        return categoryNameRepository.findAll().stream()
                .map(categoryName -> categoryName.getName().toUpperCase())
                .toList();
    }

    @Override
    public ApiResponse deleteCategoryName(CategoryNameDto categoryName) {
        categoryNameRepository.delete(
                categoryNameRepository.findByNameIgnoreCase(categoryName.getCategoryName())
                        .orElseThrow(() -> new TaskHubException("Category name could not be found"))
        );
        return apiResponse("Category name deleted successfully");
    }
}