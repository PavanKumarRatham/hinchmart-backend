package com.example.hinchmart.repository;

import com.example.hinchmart.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    List<NotificationPreference> findByUserId(Long userId);
    List<NotificationPreference> findByUserIdAndUserRole(Long userId, String userRole);
    Optional<NotificationPreference> findByUserIdAndNotificationType(Long userId, String notificationType);
}
