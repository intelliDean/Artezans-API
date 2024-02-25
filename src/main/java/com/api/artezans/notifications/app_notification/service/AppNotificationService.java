package com.api.artezans.notifications.app_notification.service;

import com.api.artezans.notifications.app_notification.model.AppNotification;

import java.util.List;

public interface AppNotificationService {
    List<AppNotification> saveNotifications(List<AppNotification> notifications);
    void saveNotifications(AppNotification notifications);
    List<AppNotification> findServiceProviderNotifications(Long userId);
}