package com.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hinchmart.entity.enums.PaymentTransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_trx_payment", columnList = "payment_id")
})
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "transactions", "refunds"})
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private PaymentTransactionType transactionType = PaymentTransactionType.PAYMENT;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "gateway_reference", length = 150)
    private String gatewayReference;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PaymentTransaction() {
    }

    public PaymentTransaction(Payment payment, PaymentTransactionType transactionType,
                              BigDecimal amount, String status, String gatewayReference, String responsePayload) {
        this.payment = payment;
        this.transactionType = transactionType;
        this.amount = amount;
        this.status = status;
        this.gatewayReference = gatewayReference;
        this.responsePayload = responsePayload;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
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

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
