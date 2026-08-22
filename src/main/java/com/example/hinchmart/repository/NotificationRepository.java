package com.example.hinchmart.repository;

import com.example.hinchmart.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<Notification> findByRecipientRoleOrderByCreatedAtDesc(String recipientRole);
    List<Notification> findByRecipientIdAndRecipientRoleOrderByCreatedAtDesc(Long recipientId, String recipientRole);
    List<Notification> findByRecipientIdAndRecipientRoleAndIsReadOrderByCreatedAtDesc(Long recipientId, String recipientRole, Boolean isRead);
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, Boolean isRead);
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    long countByRecipientIdAndRecipientRoleAndIsReadFalse(Long recipientId, String recipientRole);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsReadForRecipient(@Param("recipientId") Long recipientId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.recipientId = :recipientId AND n.recipientRole = :recipientRole AND n.isRead = false")
    int markAllAsReadForRecipientAndRole(@Param("recipientId") Long recipientId, @Param("recipientRole") String recipientRole, @Param("now") LocalDateTime now);
}
