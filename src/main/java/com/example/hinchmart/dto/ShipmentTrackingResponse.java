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
public class ShipmentTrackingResponse {
    private Long id;
    private Long shipmentId;
    private String status;
    private String location;
    private String description;
    private Double latitude;
    private Double longitude;
    private LocalDateTime eventTime;
    private String recordedBy;
}
