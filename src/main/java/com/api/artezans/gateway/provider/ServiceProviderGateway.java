package com.api.artezans.gateway.provider;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.provider.controller.ServiceProviderController;
import com.api.artezans.provider.data.dto.ServiceProviderRegistrationRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.api.artezans.gateway.provider.ServiceProviderUtil.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Service Provider Controller")
@RequestMapping("/api/v1/service_provider")
public class ServiceProviderGateway {

    private final ServiceProviderController serviceProviderController;

    @PostMapping("/sign-up")
    @Operation(summary = REGISTER_SUM, description = REGISTER_DESC, operationId = REGISTER_OP_ID)
    public ResponseEntity<ApiResponse> registerServiceProvider(
            @RequestBody @Valid final ServiceProviderRegistrationRequest request, HttpServletRequest httpRequest) {
        return new ResponseEntity<>(
                serviceProviderController.registerServiceProvider(request, httpRequest),
                HttpStatus.CREATED
        );
    }

    @PostMapping(value = "/complete", consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = COMP_REG_SUM, description = COMP_REG_DESC, operationId = COMP_REG_OP_ID)
    public ResponseEntity<ApiResponse> completeServiceProviderProfile(
            String token, @ModelAttribute @Valid ServiceProviderUpdateRequest updateRequest) {
        return ResponseEntity.ok(
                serviceProviderController.completeServiceProviderRegistration(token, updateRequest)
        );
    }

    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = PROF_PIC_SUM, description = PROF_PIC_DESC, operationId = PROF_PIC_OP_ID)
    @PostMapping(value = "/profile_picture", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadProfilePicture(@RequestBody MultipartFile image) {
        return ResponseEntity.ok(
                serviceProviderController.uploadProfilePicture(image)
        );
    }

    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @PatchMapping("update")
    @Operation(summary = UPDATE_SUM, description = UPDATE_DESC, operationId = UPDATE_OP_ID)
    public ResponseEntity<ApiResponse> updateServiceProviderInfo(
            @RequestBody JsonPatch updatePayload) {
        return ResponseEntity.ok(
                serviceProviderController.updateServiceProviderInfo(updatePayload)
        );
    }

    @GetMapping("peculiar-tasks")
    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = TASK_SUM, description = TASK_DESC, operationId = TASK_OP_ID)
    public ResponseEntity<List<Task>> serviceProviderViewPeculiarTasks() {
        return ResponseEntity.ok(
                serviceProviderController.serviceProviderViewPeculiarTasks()
        );
    }

    @GetMapping("notifications")
    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = NOTIFICATION_SUM, description = NOTIFICATION_DESC, operationId = NOTIFICATION_OP_ID)
    public ResponseEntity<List<AppNotification>> viewAllNotifications() {
        return ResponseEntity.ok(
                serviceProviderController.serviceProviderNotifications()
        );
    }
}
