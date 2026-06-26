package com.api.artezans.provider.service;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.provider.data.dto.ServiceProviderRegistrationRequest;
import com.api.artezans.provider.data.dto.ServiceProviderUpdateRequest;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import com.github.fge.jsonpatch.JsonPatch;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ServiceProviderService {

    ApiResponse registerServiceProvider(ServiceProviderRegistrationRequest request);

//    void save(ServiceProvider serviceProvider);

    // AuthResponse authenticateAndGetToken(AuthRequest authRequest);
    ApiResponse completeServiceProviderRegistration(String token, ServiceProviderUpdateRequest updateRequest);

    ServiceProvider currentServiceProvider(String emailAddress);

    ApiResponse uploadProfilePicture(MultipartFile image, User user);

    ApiResponse updateServiceProviderInfo(JsonPatch updatePayload, String emailAddress);

    boolean validateServiceProviderExistence(String email);

    List<Task> serviceProviderViewPeculiarTasks(String emailAddress);

    List<AppNotification> serviceProviderNotifications(String emailAddress);
}
