package com.api.artezans.task.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import task.hub.exceptions.TaskHubException;
import task.hub.exceptions.UserNotAuthorizedException;
import task.hub.multimedia.MultimediaService;
import task.hub.user.app_notification.model.AppNotification;
import task.hub.user.app_notification.service.AppNotificationService;
import task.hub.user.config.security.SecuredUser;
import task.hub.user.listings.data.models.Listing;
import task.hub.user.listings.services.ListingService;
import task.hub.user.task.data.dto.TaskRequest;
import task.hub.user.task.data.model.Task;
import task.hub.user.task.data.repo.TaskRepository;
import task.hub.user.users.models.User;
import task.hub.user.utils.ApiResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static task.hub.user.utils.ApiResponse.apiResponse;
import static task.hub.user.utils.TaskHubUtils.*;

@Slf4j
@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ListingService listingService;
    private final MultimediaService multimediaService;
    private final AppNotificationService appNotificationService;


    @Override
    public ApiResponse postATask(TaskRequest request) {
        try {
            User user = currentUser();
            Task newTask = createTask(user, request);
            log.info("{} posting a task for {}", user.getFirstName(), request.getTaskServiceName());

            List<AppNotification> notifications = createNotifications(user, request);
            saveNotificationsAndTask(notifications, newTask);
            log.info("{} successfully posted a task for {}", user.getFirstName(), request.getTaskServiceName());
            return apiResponse(TASK_CREATED);
        } catch (NoSuchElementException e) {
            throw new TaskHubException(CUSTOMER_NOT_FOUND);
        } catch (Exception exception) {
            throw new TaskHubException(exception.getMessage());
        }
    }

    @Override
    public List<Task> findActiveTasks() {
        return taskRepository.findAllUndeletedLists();
    }

    @Override
    public List<Task> adminViewAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public ApiResponse deletePost(Long postId) {
        Task task = taskRepository.findById(postId)
                .orElseThrow(() -> new TaskHubException("Task not found"));
        User user = currentUser();
        log.info("{} is trying to delete task: {}", user.getFirstName(), task.getId());
        if (user.getId().equals(task.getPosterId())) {
            task.setActive(false);
            taskRepository.save(task);
            return apiResponse("Task deleted successfully");
        }
        throw new UserNotAuthorizedException();
    }

    private Task createTask(User user, TaskRequest request) {
        return Task.builder()
                .posterId(user.getId())
                .taskServiceName(capitalized(request.getTaskServiceName()))
                .taskDescription(request.getTaskDescription())
                .userAddress(request.getUserAddress().isEmpty() ? user.getAddress().toString() : request.getUserAddress())
                .postedAt(LocalDateTime.now())
                .isActive(true)
                .taskDates(request.getTaskDate())
                .customerBudget(request.getCustomerBudget())
                .taskImage(uploadImage(request.getTaskImage()))
                .build();
    }

    private String uploadImage(MultipartFile image) {
        String imageUrl;
        try {
            imageUrl = multimediaService.upload(image);
        } catch (Exception ex) {
            throw new TaskHubException(ex.getMessage());
        }
        return imageUrl;
    }

    private List<AppNotification> createNotifications(User user, TaskRequest request) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Listing> listings = listingService.findListingByServiceName(request.getTaskServiceName());
        if (listings.isEmpty()) {
            return Collections.singletonList(AppNotification.builder()
                    .notificationTime(LocalDateTime.now())
                    .message("A new task for " + request.getTaskServiceName() +
                            " service has just been posted by " +
                            user.getFirstName())
                    .build());
        } else {
            return listings.parallelStream()
                    .filter(listing -> !listing.getServiceProvider().getUser().equals(user))
                    .map(list -> notifyServiceProvider(
                            list.getServiceProvider().getUser(), request.getTaskServiceName(), currentTime
                    ))
                    .collect(Collectors.toList());
        }
    }

    private AppNotification notifyServiceProvider(User user, String serviceName, LocalDateTime currentTime) {
        AppNotification notification = new AppNotification();
        notification.setMessage("You have a new " + serviceName + " request");
        notification.setNotificationTime(currentTime);
        notification.setRecipient(user);
        return notification;
    }

    private void saveNotificationsAndTask(List<AppNotification> notifications, Task newTask) {
        appNotificationService.saveNotifications(notifications);
        taskRepository.save(newTask);
    }


    private User currentUser() {
        try {
            SecuredUser securedUser = (SecuredUser) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            return securedUser.getUser();
        } catch (Exception e) {
            throw new TaskHubException("User not authenticated");
        }
    }
}