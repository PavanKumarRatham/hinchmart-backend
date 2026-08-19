package com.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hinchmart.entity.enums.DeviceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens", indexes = {
    @Index(name = "idx_device_user", columnList = "user_id"),
    @Index(name = "idx_device_token", columnList = "fcm_token")
})
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType = DeviceType.ANDROID;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DeviceToken() {
    }

    public DeviceToken(User user, String fcmToken, DeviceType deviceType) {
        this.user = user;
        this.fcmToken = fcmToken;
        this.deviceType = deviceType != null ? deviceType : DeviceType.ANDROID;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
