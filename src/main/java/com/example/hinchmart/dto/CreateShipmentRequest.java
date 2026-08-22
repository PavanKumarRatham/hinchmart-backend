package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {
    private Long deliveryPartnerId;
    private String deliveryPartnerCode; // Optional fallback if ID is omitted (e.g. BLUEDART)
    private Long sellerId;
    private String trackingNumber;
    private String shippingMode; // SURFACE_BULK, EXPRESS_LOGISTICS, DEDICATED_TRUCK
    private Double packageWeightKg;
    private String packageDimensions;
    private Integer totalPackages;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime pickupScheduledAt;
    private LocalDateTime estimatedDeliveryAt;
    private String driverName;
    private String driverContact;
    private String vehicleNumber;
    private String initialRemarks;
}
