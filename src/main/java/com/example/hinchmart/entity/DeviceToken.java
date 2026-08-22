package com.example.hinchmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_role", nullable = false)
    private String userRole; // BUYER, SELLER, ADMIN

    @Column(nullable = false, length = 500)
    private String token; // FCM Token / APNS Token / Web Push Token

    @Column(name = "device_type")
    private String deviceType; // ANDROID, IOS, WEB

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastUsedAt == null) {
            lastUsedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
