package com.hinchmart.dto.response;

import java.math.BigDecimal;

public class ProductBulkPriceDto {

    private Long id;
    private Integer minQuantity;
    private Integer maxQuantity;
    private BigDecimal pricePerUnit;
    private BigDecimal discountPercentage;
    private String tierLabel; // e.g. "1–4 Tons", "25+ Tons"

    public ProductBulkPriceDto() {
    }

    public ProductBulkPriceDto(Long id, Integer minQuantity, Integer maxQuantity, BigDecimal pricePerUnit, BigDecimal discountPercentage, String unitName) {
        this.id = id;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.pricePerUnit = pricePerUnit;
        this.discountPercentage = discountPercentage;
        if (maxQuantity != null) {
            this.tierLabel = minQuantity + "–" + maxQuantity + " " + (unitName != null ? unitName + "s" : "Units");
        } else {
            this.tierLabel = minQuantity + "+ " + (unitName != null ? unitName + "s" : "Units");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTierLabel() {
        return tierLabel;
    }

    public void setTierLabel(String tierLabel) {
        this.tierLabel = tierLabel;
    }
}
