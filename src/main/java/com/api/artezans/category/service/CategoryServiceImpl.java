package com.api.artezans.category.service;

import com.api.artezans.category.data.dtos.CategoryRequest;
import com.api.artezans.category.data.dtos.ServicesDto;
import com.api.artezans.category.data.model.Category;
import com.api.artezans.category.data.model.Services;
import com.api.artezans.category.repository.CategoryRepository;
import com.api.artezans.category.service.interfaces.CategoryService;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.users.services.UserService;
import com.api.artezans.utils.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.capitalized;


@Slf4j
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    //private final EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ApiResponse addServiceCategory(CategoryRequest request)  {
        Category category = findByCategoryName(request.getCategoryName());
      //  User user = userService.currentUser();
        log.info("CATEGORY: {}", category);

        if (category != null) {
            // Existing category, update services if needed
            List<Services> services = request.getServicesDtos().stream()
                    .filter(servicesDto -> notContained(servicesDto, category))
                    .map(servicesDto -> Services.builder()
                            .serviceName(servicesDto.getServiceName())
                            .build())
                    .toList();
            category.setServices(services);
            categoryRepository.save(category);
        } else {
            // New category
            Category newCategory = new Category();
            newCategory.setCategoryName(request.getCategoryName().toUpperCase());
            newCategory.setServices(request.getServicesDtos().stream()
                    .map(servicesDto -> Services.builder()
                            .serviceName(capitalized(servicesDto.getServiceName()))
                            .category(newCategory)
                            .build())
                    .toList());

            // Associate the user with the new category
//            User attachedUser = entityManager.merge(user);
//            newCategory.setUser(attachedUser);

            categoryRepository.save(newCategory);
        }
        return apiResponse("Service Category Added successfully");
    }


    private boolean notContained(ServicesDto servicesDto, Category category) {
        return category.getServices().stream()
                .noneMatch(service ->
                        service.getServiceName().equals(servicesDto.getServiceName()));
    }

    @Override
    public Category findByCategoryName(String categoryName) {
        return categoryRepository.findCategoryByCategoryNameIgnoreCase(categoryName)
                .orElse(null);
    }

    @Override
    public List<String> viewServicesByCategoryName(String categoryName) {
        Category category = findByCategoryName(categoryName);
        if (category != null) {
            return category.getServices().stream()
                    .map(Services::getServiceName)
                    .collect(Collectors.toList());
        }
        throw new TaskHubException("Category does not exist");
    }

    @Override
    public List<Category> viewAllCategories() {
        return categoryRepository.findAll();
    }


}