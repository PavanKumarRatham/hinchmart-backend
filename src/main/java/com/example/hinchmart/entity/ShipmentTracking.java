package com.example.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipment_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    @JsonBackReference
    private Shipment shipment;

    @Column(nullable = false)
    private String status; // PENDING, PICKUP_SCHEDULED, PICKED_UP, IN_TRANSIT, REACHED_DESTINATION, OUT_FOR_DELIVERY, DELIVERED, FAILED_DELIVERY, RETURN_TO_ORIGIN

    private String location; // e.g. Bangalore Logistics Hub, Peenya Terminal

    @Column(length = 500)
    private String description;

    private Double latitude;

    private Double longitude;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "recorded_by")
    private String recordedBy; // SYSTEM, DRIVER, SELLER, DELIVERY_PARTNER

    @PrePersist
    protected void onCreate() {
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
        if (recordedBy == null) {
            recordedBy = "SYSTEM";
        }
    }
}
