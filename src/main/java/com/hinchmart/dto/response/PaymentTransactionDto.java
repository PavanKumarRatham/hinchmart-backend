package com.hinchmart.dto.response;

import com.hinchmart.entity.enums.PaymentTransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransactionDto {

    private Long id;
    private Long paymentId;
    private PaymentTransactionType transactionType;
    private BigDecimal amount;
    private String status;
    private String gatewayReference;
    private LocalDateTime createdAt;

    public PaymentTransactionDto() {
    }

    public PaymentTransactionDto(Long id, Long paymentId, PaymentTransactionType transactionType,
                                 BigDecimal amount, String status, String gatewayReference, LocalDateTime createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.gatewayReference = gatewayReference;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(PaymentTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public void setGatewayReference(String gatewayReference) {
        this.gatewayReference = gatewayReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
