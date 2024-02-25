package com.api.artezans.notifications.app_notification.repository;

import com.api.artezans.notifications.app_notification.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    @Query(value = """
            select notification from AppNotification notification
            where notification.recipient.id = :userId
            """)
    List<AppNotification> findAllByRecipientId(Long userId);
}
