package com.hinchmart.dto.response;

public class DeliveryPartnerDto {

    private Long id;
    private String name;
    private String code;
    private String contactNumber;
    private String trackingUrlTemplate;
    private boolean isActive;

    public DeliveryPartnerDto() {
    }

    public DeliveryPartnerDto(Long id, String name, String code, String contactNumber, String trackingUrlTemplate, boolean isActive) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.contactNumber = contactNumber;
        this.trackingUrlTemplate = trackingUrlTemplate;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getTrackingUrlTemplate() {
        return trackingUrlTemplate;
    }

    public void setTrackingUrlTemplate(String trackingUrlTemplate) {
        this.trackingUrlTemplate = trackingUrlTemplate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
