package com.api.artezans.provider.service;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.provider.data.dto.ServiceProviderRegistrationRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

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
