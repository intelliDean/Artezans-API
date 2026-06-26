package com.api.artezans.category.service;

import com.api.artezans.category.data.dtos.CategoryDTO;
import com.api.artezans.category.data.dtos.CategoryMapper;
import com.api.artezans.category.data.dtos.CategoryRequest;
import com.api.artezans.category.data.dtos.ServicesDto;
import com.api.artezans.category.data.model.ArtezanService;
import com.api.artezans.category.data.model.Category;

import com.api.artezans.category.data.model.CategoryName;
import com.api.artezans.category.repository.CategoryRepository;
import com.api.artezans.category.repository.CategoryNameRepository;
import com.api.artezans.category.service.interfaces.CategoryService;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.ArtezanUtils.capitalized;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryNameRepository categoryNameRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public ApiResponse addServiceCategory(CategoryRequest request) {
        findByCategoryName(request.categoryName())
                .ifPresentOrElse(
                        category -> updateExistingCategory(category, request),
                        () -> createNewCategory(request)
                );
        return apiResponse("Service category added successfully");
    }

    private void updateExistingCategory(Category category, CategoryRequest request) {
        List<ServicesDto> newServiceDtos = request.servicesDtos().stream()
                .filter(dto -> notContained(dto, category))
                .toList();

        if (newServiceDtos.isEmpty()) {
            log.info("No new services to add to category: {}", category.getCategoryName());
            return;
        }

        for (ServicesDto dto : newServiceDtos) {
            category.addArtezanService(
                    ArtezanService.builder()
                            .serviceName(capitalized(dto.serviceName()))
                            .build()
            );
        }
        category.setUpdatedAt(java.time.LocalDateTime.now());
        categoryRepository.save(category);
        log.info("Updated category: {} with {} new services",
                category.getCategoryName(), newServiceDtos.size());
    }

    private void createNewCategory(CategoryRequest request) {
        String categoryNameUpper = request.categoryName().toUpperCase();
        Category newCategory = Category.builder()
                .categoryName(categoryNameUpper)
                .build();

        for (ServicesDto dto : request.servicesDtos()) {
            newCategory.addArtezanService(
                    ArtezanService.builder()
                            .serviceName(capitalized(dto.serviceName()))
                            .build()
            );
        }
        categoryRepository.save(newCategory);
        log.info("Created new category: {}", newCategory.getCategoryName());

        // Synchronize with category_names table
        if (!categoryNameRepository.existsByNameIgnoreCase(categoryNameUpper)) {
            categoryNameRepository.save(
                    CategoryName.builder()
                            .name(categoryNameUpper)
                            .build()
            );
            log.info("Synchronized category name '{}' to category_names table", categoryNameUpper);
        }
    }

    private boolean notContained(ServicesDto dto, Category category) {
        return category.getArtezanServices().stream()
                .noneMatch(artezanService -> artezanService.getServiceName()
                        .equalsIgnoreCase(dto.serviceName()));
    }

    @Override
    public Optional<Category> findByCategoryName(String categoryName) {
        return categoryRepository.findCategoryByCategoryNameIgnoreCase(categoryName);
    }

    @Override
    public List<String> viewServicesByCategoryName(String categoryName) {
        return findByCategoryName(categoryName)
                .map(category -> category.getArtezanServices().stream()
                        .map(ArtezanService::getServiceName)
                        .toList())
                .orElseThrow(() -> new ArtezanException("Category does not exist"));
    }

    @Override
    public List<CategoryDTO> viewAllCategories() {
        return categoryMapper.toDTOList(categoryRepository.findAll());
    }
}