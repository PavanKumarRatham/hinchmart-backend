package com.example.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_number", unique = true, nullable = false)
    private String shipmentNumber; // e.g. SHP-20260819-1001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "shipping_mode")
    private String shippingMode; // SURFACE_BULK, EXPRESS_LOGISTICS, DEDICATED_TRUCK

    @Column(name = "package_weight_kg")
    private Double packageWeightKg;

    @Column(name = "package_dimensions")
    private String packageDimensions; // e.g. 120x80x150 cm

    @Column(name = "total_packages")
    private Integer totalPackages = 1;

    @Column(nullable = false)
    private String status; // PENDING, PICKUP_SCHEDULED, PICKED_UP, IN_TRANSIT, REACHED_DESTINATION, OUT_FOR_DELIVERY, DELIVERED, FAILED_DELIVERY, RETURN_TO_ORIGIN

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "pickup_scheduled_at")
    private LocalDateTime pickupScheduledAt;

    @Column(name = "estimated_delivery_at")
    private LocalDateTime estimatedDeliveryAt;

    @Column(name = "actual_delivery_at")
    private LocalDateTime actualDeliveryAt;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_contact")
    private String driverContact;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "shipment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<ShipmentTracking> trackingHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (totalPackages == null) {
            totalPackages = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
