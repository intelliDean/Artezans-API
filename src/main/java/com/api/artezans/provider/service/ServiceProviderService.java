package com.api.artezans.provider.service;

import com.github.fge.jsonpatch.JsonPatch;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import task.hub.user.app_notification.model.AppNotification;
import task.hub.user.service.provider.data.dto.ServiceProviderRegistrationRequest;
import task.hub.user.service.provider.data.dto.ServiceProviderUpdateRequest;
import task.hub.user.service.provider.data.model.ServiceProvider;
import task.hub.user.task.data.model.Task;
import task.hub.user.utils.ApiResponse;

import java.util.List;

public interface ServiceProviderService {

    ApiResponse register(ServiceProviderRegistrationRequest request, HttpServletRequest httpRequest);
    void save(ServiceProvider serviceProvider);
   // AuthResponse authenticateAndGetToken(AuthRequest authRequest);
    ApiResponse completeServiceProviderRegistration(
            String token, ServiceProviderUpdateRequest updateRequest);
    ServiceProvider currentServiceProvider();
    ApiResponse uploadProfilePicture(MultipartFile image);
    ApiResponse updateServiceProviderInfo(JsonPatch updatePayload);
    boolean isExist(String email);
    List<Task> serviceProviderViewPeculiarTasks();
    List<AppNotification> serviceProviderNotifications();
}
