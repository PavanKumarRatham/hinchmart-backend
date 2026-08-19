package com.hinchmart.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CheckoutPreviewDto {

    private BigDecimal subtotal;
    private BigDecimal gst;
    private BigDecimal deliveryCharge;
    private BigDecimal total;
    private List<CartItemDto> items = new ArrayList<>();

    public CheckoutPreviewDto() {
    }

    public CheckoutPreviewDto(BigDecimal subtotal, BigDecimal gst, BigDecimal deliveryCharge, BigDecimal total) {
        this.subtotal = subtotal;
        this.gst = gst;
        this.deliveryCharge = deliveryCharge;
        this.total = total;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(BigDecimal deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }
}
