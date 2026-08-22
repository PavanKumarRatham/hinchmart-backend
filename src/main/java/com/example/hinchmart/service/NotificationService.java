package com.example.hinchmart.service;

import com.example.hinchmart.dto.NotificationPreferenceDTO;
import com.example.hinchmart.dto.NotificationResponse;
import com.example.hinchmart.dto.PushTokenRequest;
import com.example.hinchmart.entity.DeviceToken;
import com.example.hinchmart.entity.Notification;
import com.example.hinchmart.entity.NotificationPreference;
import com.example.hinchmart.repository.DeviceTokenRepository;
import com.example.hinchmart.repository.NotificationPreferenceRepository;
import com.example.hinchmart.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               DeviceTokenRepository deviceTokenRepository,
                               NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * Dispatch notification to user (persists in-app notification and logs push dispatch)
     */
    @Transactional
    public NotificationResponse sendNotification(Long recipientId,
                                                 String recipientRole,
                                                 String type,
                                                 String title,
                                                 String message,
                                                 String referenceType,
                                                 Long referenceId) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRecipientRole(recipientRole != null ? recipientRole.toUpperCase() : "BUYER");
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        // Check active device tokens for push notification simulation
        List<DeviceToken> activeTokens = deviceTokenRepository.findByUserIdAndIsActiveTrue(recipientId);
        if (!activeTokens.isEmpty()) {
            log.info("Dispatching Push Notification [{}] to {} device(s) for user {}: {}",
                    type, activeTokens.size(), recipientId, title);
        }

        return mapToResponse(saved);
    }

    /**
     * Fetch notifications with optional filters (recipientId, recipientRole, unreadOnly)
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long recipientId, String recipientRole, Boolean unreadOnly) {
        List<Notification> list;

        if (recipientId != null && recipientRole != null && !recipientRole.isBlank()) {
            if (Boolean.TRUE.equals(unreadOnly)) {
                list = notificationRepository.findByRecipientIdAndRecipientRoleAndIsReadOrderByCreatedAtDesc(recipientId, recipientRole.toUpperCase(), false);
            } else {
                list = notificationRepository.findByRecipientIdAndRecipientRoleOrderByCreatedAtDesc(recipientId, recipientRole.toUpperCase());
            }
        } else if (recipientId != null) {
            if (Boolean.TRUE.equals(unreadOnly)) {
                list = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, false);
            } else {
                list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
            }
        } else if (recipientRole != null && !recipientRole.isBlank()) {
            list = notificationRepository.findByRecipientRoleOrderByCreatedAtDesc(recipientRole.toUpperCase());
        } else {
            list = notificationRepository.findAll();
        }

        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Mark a single notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        Notification updated = notificationRepository.save(notification);
        return mapToResponse(updated);
    }

    /**
     * Mark all notifications as read for a recipient
     */
    @Transactional
    public int markAllAsRead(Long recipientId, String recipientRole) {
        LocalDateTime now = LocalDateTime.now();
        if (recipientRole != null && !recipientRole.isBlank()) {
            return notificationRepository.markAllAsReadForRecipientAndRole(recipientId, recipientRole.toUpperCase(), now);
        } else {
            return notificationRepository.markAllAsReadForRecipient(recipientId, now);
        }
    }

    /**
     * Register or refresh device push token
     */
    @Transactional
    public DeviceToken registerPushToken(PushTokenRequest request) {
        if (request.getUserId() == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException("UserId and Token are required to register push token.");
        }

        Optional<DeviceToken> existing = Optional.empty();
        if (request.getDeviceId() != null && !request.getDeviceId().isBlank()) {
            existing = deviceTokenRepository.findByUserIdAndDeviceId(request.getUserId(), request.getDeviceId());
        }
        if (existing.isEmpty()) {
            existing = deviceTokenRepository.findByUserIdAndToken(request.getUserId(), request.getToken());
        }

        DeviceToken deviceToken = existing.orElse(new DeviceToken());
        deviceToken.setUserId(request.getUserId());
        deviceToken.setUserRole(request.getUserRole() != null ? request.getUserRole().toUpperCase() : "BUYER");
        deviceToken.setToken(request.getToken());
        deviceToken.setDeviceType(request.getDeviceType() != null ? request.getDeviceType().toUpperCase() : "WEB");
        deviceToken.setDeviceId(request.getDeviceId());
        deviceToken.setIsActive(true);
        deviceToken.setLastUsedAt(LocalDateTime.now());
        if (deviceToken.getCreatedAt() == null) {
            deviceToken.setCreatedAt(LocalDateTime.now());
        }

        return deviceTokenRepository.save(deviceToken);
    }

    /**
     * Get notification preferences for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationPreferenceDTO> getPreferences(Long userId, String userRole) {
        List<NotificationPreference> preferences;
        if (userRole != null && !userRole.isBlank()) {
            preferences = preferenceRepository.findByUserIdAndUserRole(userId, userRole.toUpperCase());
        } else {
            preferences = preferenceRepository.findByUserId(userId);
        }
        return preferences.stream().map(this::mapToPreferenceDTO).collect(Collectors.toList());
    }

    /**
     * Update notification preference for a user
     */
    @Transactional
    public NotificationPreferenceDTO savePreference(NotificationPreferenceDTO dto) {
        if (dto.getUserId() == null || dto.getNotificationType() == null) {
            throw new IllegalArgumentException("UserId and NotificationType are required.");
        }

        NotificationPreference pref = preferenceRepository.findByUserIdAndNotificationType(dto.getUserId(), dto.getNotificationType())
                .orElse(new NotificationPreference());

        pref.setUserId(dto.getUserId());
        pref.setUserRole(dto.getUserRole() != null ? dto.getUserRole().toUpperCase() : "BUYER");
        pref.setNotificationType(dto.getNotificationType());
        pref.setInAppEnabled(dto.getInAppEnabled() != null ? dto.getInAppEnabled() : true);
        pref.setEmailEnabled(dto.getEmailEnabled() != null ? dto.getEmailEnabled() : true);
        pref.setPushEnabled(dto.getPushEnabled() != null ? dto.getPushEnabled() : true);
        pref.setSmsEnabled(dto.getSmsEnabled() != null ? dto.getSmsEnabled() : false);

        NotificationPreference saved = preferenceRepository.save(pref);
        return mapToPreferenceDTO(saved);
    }

    public long getUnreadCount(Long recipientId, String recipientRole) {
        if (recipientRole != null && !recipientRole.isBlank()) {
            return notificationRepository.countByRecipientIdAndRecipientRoleAndIsReadFalse(recipientId, recipientRole.toUpperCase());
        }
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse res = new NotificationResponse();
        res.setId(notification.getId());
        res.setRecipientId(notification.getRecipientId());
        res.setRecipientRole(notification.getRecipientRole());
        res.setType(notification.getType());
        res.setTitle(notification.getTitle());
        res.setMessage(notification.getMessage());
        res.setReferenceType(notification.getReferenceType());
        res.setReferenceId(notification.getReferenceId());
        res.setIsRead(notification.getIsRead());
        res.setReadAt(notification.getReadAt());
        res.setCreatedAt(notification.getCreatedAt());
        return res;
    }

    private NotificationPreferenceDTO mapToPreferenceDTO(NotificationPreference pref) {
        NotificationPreferenceDTO dto = new NotificationPreferenceDTO();
        dto.setId(pref.getId());
        dto.setUserId(pref.getUserId());
        dto.setUserRole(pref.getUserRole());
        dto.setNotificationType(pref.getNotificationType());
        dto.setInAppEnabled(pref.getInAppEnabled());
        dto.setEmailEnabled(pref.getEmailEnabled());
        dto.setPushEnabled(pref.getPushEnabled());
        dto.setSmsEnabled(pref.getSmsEnabled());
        return dto;
    }
}
