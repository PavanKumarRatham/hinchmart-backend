package com.hinchmart.dto.request;

import com.hinchmart.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateOrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String billingAddress;
    private String city;
    private String state;
    private String pincode;
    private PaymentMethod paymentMethod = PaymentMethod.UPI;
    private String notes;
    private List<AddToCartRequest> directItems; // Optional direct purchase without cart

    public CreateOrderRequest() {
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<AddToCartRequest> getDirectItems() {
        return directItems;
    }

    public void setDirectItems(List<AddToCartRequest> directItems) {
        this.directItems = directItems;
    }
}
