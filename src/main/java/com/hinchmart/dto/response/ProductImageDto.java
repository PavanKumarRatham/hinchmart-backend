package com.hinchmart.dto.response;

public class ProductImageDto {

    private Long id;
    private String imageUrl;
    private boolean isPrimary;
    private Integer displayOrder;

    public ProductImageDto() {
    }

    public ProductImageDto(Long id, String imageUrl, boolean isPrimary, Integer displayOrder) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
