package com.api.artezans.gateway.admin;

import com.api.artezans.admin.AdminService;
import com.api.artezans.authentication.dtos.AuthRequest;
import com.api.artezans.authentication.dtos.AuthResponse;
import com.api.artezans.category.controller.ServiceCategoryController;
import com.api.artezans.category.data.dtos.CategoryNameDto;
import com.api.artezans.category.data.dtos.CategoryRequest;
import com.api.artezans.category.data.model.Category;
import com.api.artezans.listings.controller.ListingController;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.task.controller.TaskController;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.Paginate;
import com.api.artezans.utils.SqlScriptExecutor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.api.artezans.gateway.admin.AdminUtil.*;
import static com.api.artezans.gateway.admin.ServiceCategoryUtil.*;
import static com.api.artezans.gateway.listing.ListingUtil.*;
import static com.api.artezans.gateway.task.TaskUtil.*;


@RestController
@AllArgsConstructor
@Tag(name = "Admin Controller")
@RequestMapping("api/v1/admin")
public class AdminGateway {

    private final AdminService adminService;
    private final SqlScriptExecutor sqlScriptExecutor;
    private final ServiceCategoryController serviceCategoryController;
    private final ListingController listingController;
    private final TaskController taskController;

    @PostMapping("login")
    @Operation(summary = LOGIN_SUMMARY, description = LOGIN_DESCRIPTION, operationId = LOGIN_OP_ID)
    public ResponseEntity<AuthResponse> adminLogin(@RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(
                adminService.adminLogin(authRequest)
        );
    }

    @PostMapping("script")
    @Operation(summary = "To run script")
    public void script() {
        sqlScriptExecutor.executeScript("script/test_script.sql");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("listings/{pageNumber}")
    @Operation(summary = ADMIN_LIST_SUM, description = ADMIN_LIST_DESC, operationId = ADMIN_LIST_OP_ID)
    public ResponseEntity<Paginate<Listing>> getAllListings(@PathVariable int pageNumber) {
        return ResponseEntity.ok(
                listingController.getAllListings(pageNumber)
        );
    }

    @GetMapping("admin-listing/{listingId}")
    @Operation(summary = ADMIN_BY_ID_SUM, description = ADMIN_BY_ID_DESC, operationId = ADMIN_BY_ID_OP_ID)
    public ResponseEntity<Listing> adminFindsListingById(@PathVariable Long listingId) {
        return ResponseEntity.ok(
                listingController.adminFindsListingById(listingId)
        );
    }


    @PostMapping("add-category-name")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = CAT_NAME_SUM, description = CAT_NAME_DESC, operationId = CAT_NAME_OP_ID)
    public ResponseEntity<ApiResponse> addCategoryName(@RequestBody List<CategoryNameDto> categoryNames) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceCategoryController.addCategoryName(categoryNames));
    }

    @GetMapping("all-category-names")
    @Operation(summary = ALL_CAT_NAME_SUM, description = ALL_CAT_NAME_DESC, operationId = ALL_CAT_NAME_OP_ID)
    public ResponseEntity<List<String>> getCategoryNames() {
        return ResponseEntity.ok(
                serviceCategoryController.getCategoryNames()
        );
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("add-service-category")
    @Operation(summary = SERV_CAT_NAME_SUM, description = SERV_CAT_NAME_DESC, operationId = SERV_CAT_NAME_OP_ID)
    public ResponseEntity<ApiResponse> addServiceCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceCategoryController.addServiceCategory(request));
    }

    @GetMapping("get-service-name/{categoryName}")
    @Operation(summary = BY_CAT_NAME_SUM, description = BY_CAT_NAME_DESC, operationId = BY_CAT_NAME_OP_ID)
    public ResponseEntity<List<String>> viewServicesByCategoryName(@PathVariable String categoryName) {
        return ResponseEntity.ok(
                serviceCategoryController.viewServicesByCategoryName(categoryName)
        );
    }

    @DeleteMapping("delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = DEL_CAT_NAME_SUM, description = DEL_CAT_NAME_DESC, operationId = DEL_CAT_NAME_OP_ID)
    public ResponseEntity<ApiResponse> deleteCategoryName(@RequestBody CategoryNameDto categoryName) {
        return ResponseEntity.ok(
                serviceCategoryController.deleteCategoryName(categoryName)
        );
    }

    @GetMapping("categories")
    @Operation(summary = VIEW_ALL_CATS_SUM, description = VIEW_ALL_CATS_DESC, operationId = VIEW_ALL_CATS_OP_ID)
    public ResponseEntity<List<Category>> viewCategories() {
        return ResponseEntity.ok(
                serviceCategoryController.viewAllCategories()
        );
    }

    @GetMapping("admin-view-tasks")
    @Operation(summary = ADMIN_TASK_SUM, description = ADMIN_TASK_DESC, operationId = ADMIN_TASK_OP_ID)
    public ResponseEntity<List<Task>> adminViewAllTasks() {
        return ResponseEntity.ok(taskController.adminViewAllTasks());
    }
}
