package com.hinchmart.controller;

import com.hinchmart.dto.request.RegisterDeviceTokenRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.NotificationDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Notification & Device Management (Member 2)", description = "Endpoints for In-App Notifications, Push Token Registration, and Read Status Updates")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get User Notifications", description = "Returns a paginated list of notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = authService.getCurrentUser(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/notifications/unread")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get Unread Notifications", description = "Returns all unread notifications for quick notification badge updates.")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getUnreadNotifications(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        List<NotificationDto> unread = notificationService.getUnreadNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success(unread));
    }

    @PatchMapping("/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Mark Notification as Read", description = "Marks a single notification as read.")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(Authentication authentication,
                                                                   @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        NotificationDto updated = notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", updated));
    }

    @PatchMapping("/notifications/read-all")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications for the user as read.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @PostMapping("/devices/push-token")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Register Device Push Token", description = "Registers or updates an FCM device token for receiving mobile/web push notifications.")
    public ResponseEntity<ApiResponse<Void>> registerPushToken(Authentication authentication,
                                                               @Valid @RequestBody RegisterDeviceTokenRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        notificationService.registerDeviceToken(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Device push token registered successfully", null));
    }
}
