package com.hinchmart.dto.request;

import com.hinchmart.entity.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;

public class RegisterDeviceTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    private DeviceType deviceType = DeviceType.ANDROID;

    public RegisterDeviceTokenRequest() {
    }

    public RegisterDeviceTokenRequest(String fcmToken, DeviceType deviceType) {
        this.fcmToken = fcmToken;
        this.deviceType = deviceType != null ? deviceType : DeviceType.ANDROID;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}
