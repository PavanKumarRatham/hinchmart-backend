package com.hinchmart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class RfqCreateRequest {

    @NotBlank(message = "RFQ Title is required")
    private String title;

    private String notes;

    private String deliveryPincode;

    private String deliveryCity;

    private String deliveryLocation;

    private Integer deliveryTimelineDays;

    private String requiredByDate;

    @NotEmpty(message = "At least one RFQ item is required")
    @Valid
    private List<RfqItemRequest> items = new ArrayList<>();

    public RfqCreateRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDeliveryPincode() {
        return deliveryPincode;
    }

    public void setDeliveryPincode(String deliveryPincode) {
        this.deliveryPincode = deliveryPincode;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public Integer getDeliveryTimelineDays() {
        return deliveryTimelineDays;
    }

    public void setDeliveryTimelineDays(Integer deliveryTimelineDays) {
        this.deliveryTimelineDays = deliveryTimelineDays;
    }

    public String getRequiredByDate() {
        return requiredByDate;
    }

    public void setRequiredByDate(String requiredByDate) {
        this.requiredByDate = requiredByDate;
    }

    public List<RfqItemRequest> getItems() {
        return items;
    }

    public void setItems(List<RfqItemRequest> items) {
        this.items = items;
    }
}
