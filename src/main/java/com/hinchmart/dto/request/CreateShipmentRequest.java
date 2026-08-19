package com.hinchmart.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateShipmentRequest {

    private Long deliveryPartnerId;
    private String deliveryPartnerCode;
    private String trackingNumber;
    private String awbCode;
    private String shippingLabelUrl;
    private LocalDate estimatedDeliveryDate;
    private String notes;

    public CreateShipmentRequest() {
    }

    public Long getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(Long deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
    }

    public String getDeliveryPartnerCode() {
        return deliveryPartnerCode;
    }

    public void setDeliveryPartnerCode(String deliveryPartnerCode) {
        this.deliveryPartnerCode = deliveryPartnerCode;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getAwbCode() {
        return awbCode;
    }

    public void setAwbCode(String awbCode) {
        this.awbCode = awbCode;
    }

    public String getShippingLabelUrl() {
        return shippingLabelUrl;
    }

    public void setShippingLabelUrl(String shippingLabelUrl) {
        this.shippingLabelUrl = shippingLabelUrl;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
