package com.hinchmart.dto.response;

import com.hinchmart.entity.enums.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundDto {

    private Long id;
    private String refundNumber;
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus refundStatus;
    private String gatewayRefundId;
    private LocalDateTime createdAt;

    public RefundDto() {
    }

    public RefundDto(Long id, String refundNumber, Long paymentId, Long orderId, BigDecimal amount,
                     String reason, RefundStatus refundStatus, String gatewayRefundId, LocalDateTime createdAt) {
        this.id = id;
        this.refundNumber = refundNumber;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.reason = reason;
        this.refundStatus = refundStatus;
        this.gatewayRefundId = gatewayRefundId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefundNumber() {
        return refundNumber;
    }

    public void setRefundNumber(String refundNumber) {
        this.refundNumber = refundNumber;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RefundStatus getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    public String getGatewayRefundId() {
        return gatewayRefundId;
    }

    public void setGatewayRefundId(String gatewayRefundId) {
        this.gatewayRefundId = gatewayRefundId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
