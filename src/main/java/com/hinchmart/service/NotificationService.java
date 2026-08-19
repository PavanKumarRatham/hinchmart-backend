package com.hinchmart.service;

import com.hinchmart.dto.request.RegisterDeviceTokenRequest;
import com.hinchmart.dto.response.NotificationDto;
import com.hinchmart.entity.DeviceToken;
import com.hinchmart.entity.Notification;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.DeviceType;
import com.hinchmart.entity.enums.NotificationType;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.repository.DeviceTokenRepository;
import com.hinchmart.repository.NotificationRepository;
import com.hinchmart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               DeviceTokenRepository deviceTokenRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification sendNotification(User recipient, String title, String message,
                                         NotificationType type, Long referenceId, String referenceType) {
        if (recipient == null) {
            return null;
        }

        Notification notification = new Notification(recipient, title, message, type, referenceId, referenceType);
        Notification saved = notificationRepository.save(notification);

        logger.info(">>> [NOTIFICATION DISPATCH] [{}] To: {} ({}) - Title: '{}' - Message: '{}'",
                type, recipient.getEmail(), recipient.getId(), title, message);

        // In production: trigger Firebase Cloud Messaging (FCM) push notification to registered device tokens
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(recipient.getId());
        if (!tokens.isEmpty()) {
            logger.info(">>> [PUSH DISPATCH] Triggering push to {} registered devices for user {}", tokens.size(), recipient.getId());
        }

        return saved;
    }

    @Transactional
    public Notification sendNotification(Long recipientUserId, String title, String message,
                                         NotificationType type, Long referenceId, String referenceType) {
        User recipient = userRepository.findById(recipientUserId).orElse(null);
        return sendNotification(recipient, title, message, type, referenceId, referenceType);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationDto> dtos = notifications.getContent().stream()
                .map(this::mapToNotificationDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, notifications.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToNotificationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return mapToNotificationDto(saved);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void registerDeviceToken(Long userId, RegisterDeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Optional<DeviceToken> existing = deviceTokenRepository.findByUserIdAndFcmToken(userId, request.getFcmToken());
        if (existing.isPresent()) {
            DeviceToken token = existing.get();
            token.setDeviceType(request.getDeviceType() != null ? request.getDeviceType() : DeviceType.ANDROID);
            token.setUpdatedAt(LocalDateTime.now());
            deviceTokenRepository.save(token);
        } else {
            DeviceToken newToken = new DeviceToken(user, request.getFcmToken(), request.getDeviceType());
            deviceTokenRepository.save(newToken);
        }
    }

    public NotificationDto mapToNotificationDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        if (n.getRecipient() != null) {
            dto.setRecipientId(n.getRecipient().getId());
        }
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setReferenceId(n.getReferenceId());
        dto.setReferenceType(n.getReferenceType());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
