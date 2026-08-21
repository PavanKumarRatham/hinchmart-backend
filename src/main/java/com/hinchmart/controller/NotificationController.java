package com.hinchmart.controller;

import com.hinchmart.dto.request.RegisterDeviceTokenRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.NotificationDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Notification & Device Management (Member 2)", description = "Endpoints for In-App Notifications, Push Token Registration, and Read Status Updates")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get User Notifications", description = "Returns a paginated list of notifications for the specified user.")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = authService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/notifications/unread")
    @Operation(summary = "Get Unread Notifications", description = "Returns all unread notifications for quick notification badge updates.")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnreadNotifications(@RequestParam Long userId) {
        User user = authService.getUserById(userId);
        List<NotificationDto> unread = notificationService.getUnreadNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success(unread));
    }

    @PatchMapping("/notifications/{id}/read")
    @Operation(summary = "Mark Notification as Read", description = "Marks a single notification as read.")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(@RequestParam Long userId,
                                                                   @PathVariable Long id) {
        User user = authService.getUserById(userId);
        NotificationDto updated = notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", updated));
    }

    @PatchMapping("/notifications/read-all")
    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications for the user as read.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@RequestParam Long userId) {
        User user = authService.getUserById(userId);
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @PostMapping("/devices/push-token")
    @Operation(summary = "Register Device Push Token", description = "Registers or updates an FCM device token for receiving mobile/web push notifications.")
    public ResponseEntity<ApiResponse<Void>> registerPushToken(@RequestParam Long userId,
                                                               @Valid @RequestBody RegisterDeviceTokenRequest request) {
        User user = authService.getUserById(userId);
        notificationService.registerDeviceToken(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Device push token registered successfully", null));
    }
}
