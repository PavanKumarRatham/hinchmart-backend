package com.hinchmart.dto.request;

import com.hinchmart.entity.enums.OtpPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyOtpRequest {

    @NotBlank(message = "Phone number or email is required")
    private String identifier;

    @NotBlank(message = "OTP code is required")
    @Size(min = 4, max = 10, message = "Invalid OTP code length")
    private String otpCode;

    private OtpPurpose purpose = OtpPurpose.LOGIN;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String identifier, String otpCode, OtpPurpose purpose) {
        this.identifier = identifier;
        this.otpCode = otpCode;
        this.purpose = purpose != null ? purpose : OtpPurpose.LOGIN;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(OtpPurpose purpose) {
        this.purpose = purpose;
    }
}
