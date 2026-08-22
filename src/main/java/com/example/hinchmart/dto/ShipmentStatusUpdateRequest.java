package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusUpdateRequest {
    private String status; // PENDING, PICKUP_SCHEDULED, PICKED_UP, IN_TRANSIT, REACHED_DESTINATION, OUT_FOR_DELIVERY, DELIVERED, FAILED_DELIVERY, RETURN_TO_ORIGIN
    private String location; // e.g. "Peenya Fulfillment Hub, Bangalore"
    private String description; // e.g. "Package received at regional hub"
    private Double latitude;
    private Double longitude;
    private String updatedBy; // SELLER, DELIVERY_PARTNER, DRIVER, SYSTEM
}
