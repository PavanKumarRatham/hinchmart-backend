package com.hinchmart.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDto {

    private Long id;
    private Long buyerId;
    private List<CartItemDto> items = new ArrayList<>();
    private int totalItems;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal gstTotal = BigDecimal.ZERO;
    private BigDecimal deliveryCharge = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;

    public CartDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getGstTotal() {
        return gstTotal;
    }

    public void setGstTotal(BigDecimal gstTotal) {
        this.gstTotal = gstTotal;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(BigDecimal deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }
}
