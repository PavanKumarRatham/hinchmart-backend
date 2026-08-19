package com.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notif_pref_user", columnList = "user_id")
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = true;

    @Column(name = "order_updates", nullable = false)
    private boolean orderUpdates = true;

    @Column(name = "rfq_updates", nullable = false)
    private boolean rfqUpdates = true;

    @Column(name = "promotional", nullable = false)
    private boolean promotional = false;

    public NotificationPreference() {
    }

    public NotificationPreference(User user) {
        this.user = user;
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

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public boolean isOrderUpdates() {
        return orderUpdates;
    }

    public void setOrderUpdates(boolean orderUpdates) {
        this.orderUpdates = orderUpdates;
    }

    public boolean isRfqUpdates() {
        return rfqUpdates;
    }

    public void setRfqUpdates(boolean rfqUpdates) {
        this.rfqUpdates = rfqUpdates;
    }

    public boolean isPromotional() {
        return promotional;
    }

    public void setPromotional(boolean promotional) {
        this.promotional = promotional;
    }
}
