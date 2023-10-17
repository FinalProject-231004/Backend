package com.starta.project.domain.notification.repository;

import com.starta.project.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByReceiver(String receiver);

    @Modifying
    @Query("update Notification set readYn = 'Y' where receiver = :username")
    void bulkReadUpdate(@Param("username") String username);

    @Modifying
    @Query("update Notification set deletedYn = 'Y' where notificationId = :notificationId")
    void bulkDeletedUpdate(String notificationId);

    Notification findByNotificationId(String notificationId);
}
