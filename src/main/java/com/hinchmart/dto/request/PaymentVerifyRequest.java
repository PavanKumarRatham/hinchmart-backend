package com.hinchmart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentVerifyRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    private Long orderId;

    @NotBlank(message = "Gateway Payment ID is required")
    private String gatewayPaymentId;

    private String gatewayOrderId;
    private String gatewaySignature;

    public PaymentVerifyRequest() {
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

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public void setGatewayPaymentId(String gatewayPaymentId) {
        this.gatewayPaymentId = gatewayPaymentId;
    }

    public String getGatewayOrderId() {
        return gatewayOrderId;
    }

    public void setGatewayOrderId(String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }

    public String getGatewaySignature() {
        return gatewaySignature;
    }

    public void setGatewaySignature(String gatewaySignature) {
        this.gatewaySignature = gatewaySignature;
    }
}
