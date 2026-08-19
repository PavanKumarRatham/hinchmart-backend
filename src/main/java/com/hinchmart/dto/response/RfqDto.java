package com.hinchmart.dto.response;

import com.hinchmart.entity.enums.RfqStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RfqDto {

    private Long id;
    private String rfqNumber;
    private Long buyerId;
    private String buyerName;
    private String buyerCompanyName;
    private String buyerEmail;
    private String buyerPhone;
    private String title;
    private String notes;
    private String deliveryPincode;
    private String deliveryCity;
    private String deliveryLocation;
    private Integer deliveryTimelineDays;
    private String requiredByDate;
    private RfqStatus status;
    private List<RfqItemDto> items = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RfqDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRfqNumber() {
        return rfqNumber;
    }

    public void setRfqNumber(String rfqNumber) {
        this.rfqNumber = rfqNumber;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerCompanyName() {
        return buyerCompanyName;
    }

    public void setBuyerCompanyName(String buyerCompanyName) {
        this.buyerCompanyName = buyerCompanyName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
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

    public RfqStatus getStatus() {
        return status;
    }

    public void setStatus(RfqStatus status) {
        this.status = status;
    }

    public List<RfqItemDto> getItems() {
        return items;
    }

    public void setItems(List<RfqItemDto> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
