package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long cartId;
    private Long buyerId;
    private Integer totalItems; // Sum of quantities
    private Integer totalLineItems; // Count of distinct products
    private Double subtotalAmount; // Excl. GST
    private Double totalGstAmount; // Total GST across items
    private Double grandTotal; // subtotal + total GST
    private String currency; // "INR"
    private List<CartItemResponse> items = new ArrayList<>();

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalLineItems() {
        return totalLineItems;
    }

    public void setTotalLineItems(Integer totalLineItems) {
        this.totalLineItems = totalLineItems;
    }

    public Double getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(Double subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public Double getTotalGstAmount() {
        return totalGstAmount;
    }

    public void setTotalGstAmount(Double totalGstAmount) {
        this.totalGstAmount = totalGstAmount;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }
}
