package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private Long id;
    private Long userId;
    private String userRole;
    private String notificationType;
    private Boolean inAppEnabled;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean smsEnabled;
}
