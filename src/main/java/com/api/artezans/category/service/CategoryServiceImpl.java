package com.api.artezans.category.service;

import com.api.artezans.category.data.dtos.CategoryRequest;
import com.api.artezans.category.data.dtos.ServicesDto;
import com.api.artezans.category.data.model.Category;

import com.api.artezans.category.repository.CategoryRepository;
import com.api.artezans.category.service.interfaces.CategoryService;
import com.api.artezans.exceptions.ArtezanException;
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
import static com.api.artezans.utils.ArtezanUtils.capitalized;


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

        Category category = findByCategoryName(request.categoryName());
      //  User user = userService.currentUser();
        log.info("CATEGORY: {}", category);

        if (category != null) {
            // Existing category, update services if needed
            List<com.api.artezans.category.data.model.Service> services = request.servicesDtos().stream()
                    .filter(servicesDto -> notContained(servicesDto, category))
                    .map(servicesDto -> com.api.artezans.category.data.model.Service.builder()
                            .serviceName(servicesDto.serviceName())
                            .build())
                    .toList();
            category.setServices(services);
            categoryRepository.save(category);
        } else {

            // New category
            Category newCategory = new Category();
            newCategory.setCategoryName(request.categoryName().toUpperCase());
            newCategory.setServices(request.servicesDtos().stream()
                    .map(servicesDto -> com.api.artezans.category.data.model.Service.builder()
                            .serviceName(capitalized(servicesDto.serviceName()))
                            .category(newCategory)
                            .build())
                    .toList()
            );

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
                        service.getServiceName().equals(servicesDto.serviceName()));
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
                    .map(com.api.artezans.category.data.model.Service::getServiceName)
                    .collect(Collectors.toList());
        }
        throw new ArtezanException("Category does not exist");
    }

    @Override
    public List<Category> viewAllCategories() {
        return categoryRepository.findAll();
    }
}