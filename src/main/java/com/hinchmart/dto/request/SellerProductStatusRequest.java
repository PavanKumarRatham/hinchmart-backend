package com.hinchmart.dto.request;

import jakarta.validation.constraints.NotNull;

public class SellerProductStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;

    public SellerProductStatusRequest() {
    }

    public SellerProductStatusRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
