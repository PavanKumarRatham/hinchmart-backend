package com.hinchmart.dto.request;

import com.hinchmart.entity.enums.SellerStatus;
import jakarta.validation.constraints.NotNull;

public class SellerStatusUpdateRequest {

    @NotNull(message = "Seller status is required")
    private SellerStatus status;

    private String rejectionReason;

    public SellerStatusUpdateRequest() {
    }

    public SellerStatusUpdateRequest(SellerStatus status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public void setStatus(SellerStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
