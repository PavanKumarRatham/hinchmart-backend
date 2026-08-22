package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long recipientId;
    private String recipientRole;
    private String type; // ORDER_PLACED, ORDER_CONFIRMED, PAYMENT_SUCCESS, PAYMENT_FAILED, PRODUCT_APPROVED, PRODUCT_REJECTED, RFQ_RECEIVED, QUOTE_RECEIVED, QUOTE_ACCEPTED, ORDER_SHIPPED, OUT_FOR_DELIVERY, ORDER_DELIVERED, LOW_STOCK
    private String title;
    private String message;
    private String referenceType;
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
