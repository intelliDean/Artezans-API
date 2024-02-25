package com.api.artezans.provider.controller;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.provider.data.dto.ServiceProviderRegistrationRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.provider.service.ServiceProviderService;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Slf4j
@Component
@AllArgsConstructor
public class ServiceProviderController {

    private final ServiceProviderService serviceProviderService;

    public ApiResponse registerServiceProvider(
            @Valid ServiceProviderRegistrationRequest request, HttpServletRequest httpRequest) {
        return serviceProviderService.register(request, httpRequest);
    }

//    public AuthResponse authenticateAndGetToken(AuthRequest authRequest) {
//        if (serviceProviderService.isExist(authRequest.getEmailAddress())) {
//            return serviceProviderService.authenticateAndGetToken(authRequest);
//        }
//        throw new UserNotAuthorizedException();
//    }

    public ApiResponse completeServiceProviderRegistration(
            String token, ServiceProviderUpdateRequest updateRequest) {
        return serviceProviderService.completeServiceProviderRegistration(token, updateRequest);
    }

    public ApiResponse uploadProfilePicture(MultipartFile image) {
        return serviceProviderService.uploadProfilePicture(image);
    }

    public ApiResponse updateServiceProviderInfo(JsonPatch updatePayload) {
        return serviceProviderService.updateServiceProviderInfo(updatePayload);
    }
    public List<Task> serviceProviderViewPeculiarTasks() {
        return serviceProviderService.serviceProviderViewPeculiarTasks();
    }
    public List<AppNotification> serviceProviderNotifications() {
        return serviceProviderService.serviceProviderNotifications();
    }
}