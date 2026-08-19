package com.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hinchmart.entity.enums.StoreStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_stores", indexes = {
    @Index(name = "idx_store_seller", columnList = "seller_id"),
    @Index(name = "idx_store_slug", columnList = "store_slug"),
    @Index(name = "idx_store_status", columnList = "status")
})
public class SellerStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User seller;

    @Column(name = "store_name", nullable = false, length = 150)
    private String storeName;

    @Column(name = "store_slug", nullable = false, unique = true, length = 180)
    private String storeSlug;

    @Column(length = 500)
    private String logo;

    @Column(length = 500)
    private String banner;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_email", length = 150)
    private String businessEmail;

    @Column(name = "business_mobile", length = 20)
    private String businessMobile;

    @Column(length = 30)
    private String gstin;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StoreStatus status = StoreStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SellerStore() {
    }

    public SellerStore(User seller, String storeName, String storeSlug, String logo, String banner,
                       String description, String businessEmail, String businessMobile, String gstin,
                       String address, StoreStatus status) {
        this.seller = seller;
        this.storeName = storeName;
        this.storeSlug = storeSlug;
        this.logo = logo;
        this.banner = banner;
        this.description = description;
        this.businessEmail = businessEmail;
        this.businessMobile = businessMobile;
        this.gstin = gstin;
        this.address = address;
        this.status = status != null ? status : StoreStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreSlug() {
        return storeSlug;
    }

    public void setStoreSlug(String storeSlug) {
        this.storeSlug = storeSlug;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getBusinessMobile() {
        return businessMobile;
    }

    public void setBusinessMobile(String businessMobile) {
        this.businessMobile = businessMobile;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public StoreStatus getStatus() {
        return status;
    }

    public void setStatus(StoreStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
