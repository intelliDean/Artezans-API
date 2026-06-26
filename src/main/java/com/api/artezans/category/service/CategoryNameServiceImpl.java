package com.api.artezans.category.service;


import com.api.artezans.category.data.dtos.CategoryNameDto;
import com.api.artezans.category.data.model.Category;
import com.api.artezans.category.data.model.CategoryName;
import com.api.artezans.category.repository.CategoryRepository;
import com.api.artezans.category.repository.CategoryNameRepository;
import com.api.artezans.category.service.interfaces.CategoryNameService;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.api.artezans.utils.ApiResponse.apiResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryNameServiceImpl implements CategoryNameService {

    private final CategoryNameRepository categoryNameRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ApiResponse addCategoryName(List<CategoryNameDto> categoryNameDtos) {
        Set<String> requestedNames = categoryNameDtos.stream()
                .map(dto -> dto.categoryName().toUpperCase())
                .collect(Collectors.toSet());

        Set<String> existingNames = categoryNameRepository.findExistingNames(requestedNames);

        List<CategoryName> newNames = requestedNames.stream()
                .filter(name -> !existingNames.contains(name))
                .map(name -> CategoryName.builder().name(name).build())
                .toList();

        if (newNames.isEmpty()) {
            throw new ArtezanException("All provided category names already exist");
        }

        categoryNameRepository.saveAll(newNames);

        int skipped = requestedNames.size() - newNames.size();
        String message = skipped > 0
                ? "%d category name(s) saved. %d already existed and were skipped"
                .formatted(newNames.size(), skipped)
                : "Category name(s) saved successfully";

        log.info(message);
        return apiResponse(message);
    }

    @Override
    public List<String> findAllCategoryNames() {
        return Stream.concat( //this is done at DB level
                        categoryNameRepository.findAllNames().stream(), //all the names in category name
                        categoryRepository.findAllCategoryNames().stream() //all the names in category
                )
                .map(String::toUpperCase)
                .distinct()
                .sorted()
                .toList();
    }


    @Override
    public ApiResponse deleteCategoryName(CategoryNameDto categoryName) {
        categoryNameRepository.delete(
                categoryNameRepository.findByNameIgnoreCase(categoryName.categoryName())
                        .orElseThrow(() -> new ArtezanException("Category name could not be found"))
        );
        log.info("Category name deleted: {}", categoryName.categoryName());
        return apiResponse("Category name deleted successfully");
    }
}