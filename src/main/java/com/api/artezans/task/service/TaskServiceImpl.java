package com.api.artezans.task.service;

import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.exceptions.UserNotAuthorizedException;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.services.ListingService;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.notifications.app_notification.service.AppNotificationService;
import com.api.artezans.task.data.dto.TaskRequest;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.task.data.repo.TaskRepository;
import com.api.artezans.users.models.Address;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.ArtezanUtils.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ListingService listingService;
    private final MultimediaService multimediaService;
    private final AppNotificationService appNotificationService;

    @Override
    @Transactional
    public ApiResponse postATask(TaskRequest request, User user) {
        Task newTask = createTask(user, request);
        log.info("{} posting a task for {}", user.getFirstName(), request.taskServiceName());

        List<AppNotification> notifications = createNotifications(user, request);
        saveNotificationsAndTask(notifications, newTask);
        log.info("{} successfully posted a task for {}", user.getFirstName(), request.taskServiceName());
        return apiResponse(TASK_CREATED);
    }

    @Override
    public List<Task> findActiveTasks() {
        return taskRepository.findAllByIsActiveTrue();
    }

    @Override
    public List<Task> adminViewAllTasks() {
        return taskRepository.findAll(); // intentionally includes inactive tasks
    }

    @Override
    public ApiResponse deactivateTask(Long postId, User user) {
        Task task = taskRepository.findById(postId)
                .orElseThrow(() -> new ArtezanException("Task not found"));

        log.info("{} is trying to deactivate task: {}", user.getFirstName(), task.getId());

        if (!user.getId().equals(task.getPoster().getId())) {
            throw new UserNotAuthorizedException();
        }

        task.setActive(false);
        taskRepository.save(task);
        return apiResponse("Task deactivated successfully");
    }

    @Override
    public List<Task> findTasksByPoster(User user) {
        return taskRepository.findAllByPoster(user);
    }

    private Task createTask(User user, TaskRequest request) {
        return Task.builder()
                .poster(user)
                .taskServiceName(capitalized(request.taskServiceName()))
                .taskDescription(request.taskDescription())
                .userAddress(StringUtils.hasText(request.userAddress())
                        ? request.userAddress()
                        : Optional.ofNullable(user.getAddress())
                        .map(Address::toString)
                        .orElse(""))
                .isActive(true)
                .taskDates(request.taskDate())
                .customerBudget(request.customerBudget())
                .taskImageUrl(uploadImage(request.taskImage()))
                .build();
    }

    private String uploadImage(MultipartFile image) {
        try {
            return multimediaService.upload(image);
        } catch (Exception ex) {
            throw new ArtezanException("Image upload failed: " + ex.getMessage());
        }
    }

    private List<AppNotification> createNotifications(User user, TaskRequest request) {
        List<Listing> listings = listingService.findAllListingsByServiceName(request.taskServiceName());

        if (listings.isEmpty()) {
            log.info("No service providers found for service: {}", request.taskServiceName());
            return Collections.emptyList();
        }

        //notify all service providers with peculiar listing
        return listings.stream()
                .filter(listing -> !listing.getServiceProvider().getUser().equals(user))
                .map(listing -> notifyServiceProvider(
                        listing.getServiceProvider().getUser(),
                        request.taskServiceName(),
                        LocalDateTime.now()
                ))
                .toList();
    }

    private AppNotification notifyServiceProvider(User user, String serviceName, LocalDateTime time) {
        return AppNotification.builder()
                .message("You have a new " + serviceName + " request")
                .notificationTime(time)
                .recipient(user)
                .build();
    }

    private void saveNotificationsAndTask(List<AppNotification> notifications, Task newTask) {
        appNotificationService.saveNotifications(notifications);
        taskRepository.save(newTask);
    }
}