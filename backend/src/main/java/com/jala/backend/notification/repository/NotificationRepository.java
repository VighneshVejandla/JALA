package com.jala.backend.notification.repository;

import com.jala.backend.notification.entity.Notification;
import com.jala.backend.notification.enums.NotificationStatus;
import com.jala.backend.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByStatusOrderByCreatedAtDesc(
            NotificationStatus status);

    long countByStatus(
            NotificationStatus status);

    boolean existsByTypeAndSiteIdAndStatus(
            NotificationType type,
            UUID siteId,
            NotificationStatus status);

    long countBySiteIdAndStatus(
            UUID siteId,
            NotificationStatus status);

    @Query("""
        SELECT n
        FROM Notification n
        WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(n.message) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY n.createdAt DESC
        """)
    List<Notification> search(
            String keyword);
}