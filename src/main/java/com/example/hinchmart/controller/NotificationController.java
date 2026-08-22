package com.example.hinchmart.controller;

import com.example.hinchmart.dto.NotificationPreferenceDTO;
import com.example.hinchmart.dto.NotificationResponse;
import com.example.hinchmart.dto.PushTokenRequest;
import com.example.hinchmart.entity.DeviceToken;
import com.example.hinchmart.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications
     * Retrieve notifications for user (with optional filters: userId, role, unreadOnly).
     * If no parameters are provided, returns all notifications.
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean unreadOnly) {
        List<NotificationResponse> list = notificationService.getNotifications(userId, role, unreadOnly);
        return ResponseEntity.ok(list);
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Mark a single notification as read
     */
    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable("id") Long id) {
        try {
            NotificationResponse response = notificationService.markAsRead(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/notifications/read-all
     * Mark all notifications as read for a user
     */
    @PatchMapping("/notifications/read-all")
    public ResponseEntity<?> markAllAsRead(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam(required = false) String role) {
        int count = notificationService.markAllAsRead(userId, role);
        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read.",
                "markedCount", count
        ));
    }

    /**
     * POST /api/devices/push-token
     * Register or update a device push token for push notifications
     */
    @PostMapping("/devices/push-token")
    public ResponseEntity<?> registerPushToken(@RequestBody PushTokenRequest request) {
        try {
            DeviceToken deviceToken = notificationService.registerPushToken(request);
            return new ResponseEntity<>(Map.of(
                    "message", "Push token registered successfully.",
                    "tokenId", deviceToken.getId(),
                    "deviceType", deviceToken.getDeviceType(),
                    "userId", deviceToken.getUserId()
            ), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/notifications/unread-count
     * Count unread notifications for badge counters
     */
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadCount(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam(required = false, defaultValue = "BUYER") String role) {
        long count = notificationService.getUnreadCount(userId, role);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * GET /api/notifications/preferences
     * Get user notification preferences
     */
    @GetMapping("/notifications/preferences")
    public ResponseEntity<List<NotificationPreferenceDTO>> getPreferences(
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam(required = false, defaultValue = "BUYER") String role) {
        List<NotificationPreferenceDTO> preferences = notificationService.getPreferences(userId, role);
        return ResponseEntity.ok(preferences);
    }

    /**
     * PUT /api/notifications/preferences
     * Update user notification preferences
     */
    @PutMapping("/notifications/preferences")
    public ResponseEntity<?> savePreference(@RequestBody NotificationPreferenceDTO dto) {
        try {
            NotificationPreferenceDTO saved = notificationService.savePreference(dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
