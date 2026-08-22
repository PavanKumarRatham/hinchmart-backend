package com.example.hinchmart.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RfqItemRequest {

    private Long productId;

    private Integer quantity;

    private String unit;

    private String remarks;
    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getRemarks() {
        return remarks;
    }
}
