package com.hinchmart.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String slug;

    private String description;

    private String imageUrl;

    private Integer displayOrder = 0;

    private Boolean active = true;

    public CategoryCreateRequest() {
    }

    public CategoryCreateRequest(String name, String slug, String description, String imageUrl, Integer displayOrder) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
