package com.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hinchmart.entity.enums.SellerStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seller_profiles", indexes = {
    @Index(name = "idx_seller_status", columnList = "status"),
    @Index(name = "idx_seller_gstin", columnList = "gstin")
})
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(length = 20)
    private String gstin;

    @Column(name = "pan_number", length = 20)
    private String panNumber;

    @Column(name = "business_type", length = 50)
    private String businessType; // Manufacturer, Authorized Distributor, Wholesaler, Stockist

    @Column(name = "warehouse_address", columnDefinition = "TEXT")
    private String warehouseAddress;

    @Column(length = 60)
    private String city;

    @Column(length = 60)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(name = "rating")
    private Double rating = 4.5;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SellerStatus status = SellerStatus.PENDING;

    @Column(name = "bank_account_number", length = 35)
    private String bankAccountNumber;

    @Column(name = "bank_ifsc_code", length = 20)
    private String bankIfscCode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_name", length = 150)
    private String bankAccountName;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @OneToMany(mappedBy = "sellerProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SellerDocument> documents = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SellerProfile() {
    }

    public SellerProfile(User user, String companyName, String gstin, String businessType, SellerStatus status) {
        this.user = user;
        this.companyName = companyName;
        this.gstin = gstin;
        this.businessType = businessType;
        this.status = status != null ? status : SellerStatus.PENDING;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getWarehouseAddress() {
        return warehouseAddress;
    }

    public void setWarehouseAddress(String warehouseAddress) {
        this.warehouseAddress = warehouseAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public SellerStatus getStatus() {
        return status;
    }

    public void setStatus(SellerStatus status) {
        this.status = status;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankIfscCode() {
        return bankIfscCode;
    }

    public void setBankIfscCode(String bankIfscCode) {
        this.bankIfscCode = bankIfscCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public List<SellerDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<SellerDocument> documents) {
        this.documents = documents;
    }

    public void addDocument(SellerDocument document) {
        documents.add(document);
        document.setSellerProfile(this);
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
