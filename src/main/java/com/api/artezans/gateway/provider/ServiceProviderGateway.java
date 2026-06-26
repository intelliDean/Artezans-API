package com.api.artezans.gateway.provider;

import com.api.artezans.config.annotation.CurrentUser;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.provider.data.dto.ServiceProviderRegistrationRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.provider.service.ServiceProviderService;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final ServiceProviderService serviceProviderService;

    @PostMapping("/sign-up")
    @Operation(summary = REGISTER_SUM, description = REGISTER_DESC, operationId = REGISTER_OP_ID)
    public ResponseEntity<ApiResponse> registerServiceProvider(
            @RequestBody @Valid final ServiceProviderRegistrationRequest request) {
        return new ResponseEntity<>(
                serviceProviderService.registerServiceProvider(request),
                HttpStatus.CREATED
        );
    }

    @PostMapping(value = "/complete", consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = COMP_REG_SUM, description = COMP_REG_DESC, operationId = COMP_REG_OP_ID)
    public ResponseEntity<ApiResponse> completeServiceProviderProfile(
            String token, @ModelAttribute @Valid ServiceProviderUpdateRequest updateRequest) {
        return ResponseEntity.ok(
                serviceProviderService.completeServiceProviderRegistration(token, updateRequest)
        );
    }

    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = PROF_PIC_SUM, description = PROF_PIC_DESC, operationId = PROF_PIC_OP_ID)
    @PostMapping(value = "/profile_picture", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadProfilePicture(@RequestBody MultipartFile image, @CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(
                serviceProviderService.uploadProfilePicture(image, currentUser.getUser())
        );
    }

    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @PatchMapping("update")
    @Operation(summary = UPDATE_SUM, description = UPDATE_DESC, operationId = UPDATE_OP_ID)
    public ResponseEntity<ApiResponse> updateServiceProviderInfo(
            @RequestBody JsonPatch updatePayload, @CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(
                serviceProviderService.updateServiceProviderInfo(updatePayload, currentUser.getUser().getEmailAddress())
        );
    }

    @GetMapping("peculiar-tasks")
    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = TASK_SUM, description = TASK_DESC, operationId = TASK_OP_ID)
    public ResponseEntity<List<Task>> serviceProviderViewPeculiarTasks(@CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(
                serviceProviderService.serviceProviderViewPeculiarTasks(currentUser.getUser().getEmailAddress())
        );
    }

    @GetMapping("notifications")
    @PreAuthorize("hasAuthority('SERVICE_PROVIDER')")
    @Operation(summary = NOTIFICATION_SUM, description = NOTIFICATION_DESC, operationId = NOTIFICATION_OP_ID)
    public ResponseEntity<List<AppNotification>> viewAllNotifications(@CurrentUser SecuredUser currentUser) {
        return ResponseEntity.ok(
                serviceProviderService.serviceProviderNotifications(currentUser.getUser().getEmailAddress())
        );
    }
}
