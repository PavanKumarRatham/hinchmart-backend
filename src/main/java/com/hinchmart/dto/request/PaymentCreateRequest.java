package com.hinchmart.dto.request;

import com.hinchmart.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class PaymentCreateRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private PaymentMethod paymentMethod = PaymentMethod.UPI;

    public PaymentCreateRequest() {
    }

    public PaymentCreateRequest(Long orderId, PaymentMethod paymentMethod) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.UPI;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
