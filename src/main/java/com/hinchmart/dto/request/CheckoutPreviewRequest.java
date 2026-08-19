package com.hinchmart.dto.request;

import java.util.List;

public class CheckoutPreviewRequest {

    private String shippingAddress;
    private String pincode;
    private List<AddToCartRequest> items; // Optional direct items

    public CheckoutPreviewRequest() {
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public List<AddToCartRequest> getItems() {
        return items;
    }

    public void setItems(List<AddToCartRequest> items) {
        this.items = items;
    }
}
