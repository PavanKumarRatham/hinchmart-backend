package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PushTokenRequest {
    private Long userId;
    private String userRole; // BUYER, SELLER, ADMIN
    private String token;
    private String deviceType; // ANDROID, IOS, WEB
    private String deviceId;
}
