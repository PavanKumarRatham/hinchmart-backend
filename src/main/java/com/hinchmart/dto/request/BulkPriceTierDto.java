package com.hinchmart.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BulkPriceTierDto {

    @NotNull(message = "Minimum quantity is required")
    private Integer minQuantity;

    private Integer maxQuantity; // null means unbounded (e.g. 25+)

    @NotNull(message = "Price per unit is required")
    private BigDecimal pricePerUnit;

    private BigDecimal discountPercentage;

    public BulkPriceTierDto() {
    }

    public BulkPriceTierDto(Integer minQuantity, Integer maxQuantity, BigDecimal pricePerUnit, BigDecimal discountPercentage) {
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.pricePerUnit = pricePerUnit;
        this.discountPercentage = discountPercentage;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
