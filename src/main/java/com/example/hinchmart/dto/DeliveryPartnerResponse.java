package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerResponse {
    private Long id;
    private String partnerName;
    private String partnerCode;
    private String contactNumber;
    private String trackingUrlTemplate;
    private String vehicleType;
    private Boolean active;
}
