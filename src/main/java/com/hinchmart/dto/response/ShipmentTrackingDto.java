package com.hinchmart.dto.response;

import com.hinchmart.entity.enums.ShipmentStatus;
import java.time.LocalDateTime;

public class ShipmentTrackingDto {

    private Long id;
    private Long shipmentId;
    private ShipmentStatus status;
    private String location;
    private String description;
    private LocalDateTime timestamp;

    public ShipmentTrackingDto() {
    }

    public ShipmentTrackingDto(Long id, Long shipmentId, ShipmentStatus status,
                               String location, String description, LocalDateTime timestamp) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.status = status;
        this.location = location;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
