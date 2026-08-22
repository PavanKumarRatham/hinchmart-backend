package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private String shipmentNumber;
    private Long orderId;
    private String orderNumber;
    private Long sellerId;
    private DeliveryPartnerResponse deliveryPartner;
    private String trackingNumber;
    private String trackingUrl;
    private String shippingMode;
    private Double packageWeightKg;
    private String packageDimensions;
    private Integer totalPackages;
    private String status;
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime pickupScheduledAt;
    private LocalDateTime estimatedDeliveryAt;
    private LocalDateTime actualDeliveryAt;
    private String driverName;
    private String driverContact;
    private String vehicleNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ShipmentTrackingResponse> trackingHistory = new ArrayList<>();
}
