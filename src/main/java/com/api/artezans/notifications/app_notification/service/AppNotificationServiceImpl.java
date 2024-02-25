package com.api.artezans.notifications.app_notification.service;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import com.api.artezans.notifications.app_notification.repository.AppNotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@AllArgsConstructor
public class AppNotificationServiceImpl implements AppNotificationService{
    private final AppNotificationRepository appNotificationRepository;

    @Override
    public List<AppNotification> saveNotifications(List<AppNotification> notifications) {
        return appNotificationRepository.saveAll(notifications);
    }

    @Override
    public void saveNotifications(AppNotification notification) {
        appNotificationRepository.save(notification);
    }

    @Override
    public List<AppNotification> findServiceProviderNotifications(Long userId) {
        return appNotificationRepository.findAllByRecipientId(userId);
    }
}
