package com.example.hinchmart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_name", nullable = false)
    private String partnerName; // e.g. BlueDart Express, Delhivery Surface, VRL Logistics, Rivigo Heavy, GATI KWE

    @Column(name = "partner_code", unique = true, nullable = false)
    private String partnerCode; // e.g. BLUEDART, DELHIVERY, VRL, RIVIGO, GATI

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "tracking_url_template")
    private String trackingUrlTemplate; // e.g. https://track.bluedart.com/track?no={tracking_number}

    @Column(name = "vehicle_type")
    private String vehicleType; // HEAVY_TRUCK_32FT, CONTAINER_20FT, MINI_TRUCK_EICHER, EXPRESS_VAN

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }
}
