package com.example.hinchmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId; // User ID (Buyer, Seller, Admin)

    @Column(name = "recipient_role", nullable = false)
    private String recipientRole; // BUYER, SELLER, ADMIN

    @Column(nullable = false)
    private String type; // ORDER_PLACED, ORDER_CONFIRMED, PAYMENT_SUCCESS, PAYMENT_FAILED, PRODUCT_APPROVED, PRODUCT_REJECTED, RFQ_RECEIVED, QUOTE_RECEIVED, QUOTE_ACCEPTED, ORDER_SHIPPED, OUT_FOR_DELIVERY, ORDER_DELIVERED, LOW_STOCK

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "reference_type")
    private String referenceType; // ORDER, SHIPMENT, PRODUCT, RFQ, QUOTE, PAYMENT

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
