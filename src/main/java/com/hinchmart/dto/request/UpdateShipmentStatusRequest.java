package com.hinchmart.dto.request;

import com.hinchmart.entity.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateShipmentStatusRequest {

    @NotNull(message = "Shipment status is required")
    private ShipmentStatus status;

    private String location;
    private String description;

    public UpdateShipmentStatusRequest() {
    }

    public UpdateShipmentStatusRequest(ShipmentStatus status, String location, String description) {
        this.status = status;
        this.location = location;
        this.description = description;
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
}
