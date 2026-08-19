package com.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hinchmart.entity.enums.ApprovalStatus;
import com.hinchmart.entity.enums.DocumentType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_documents", indexes = {
    @Index(name = "idx_doc_seller", columnList = "seller_profile_id")
})
public class SellerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_profile_id", nullable = false)
    @JsonIgnore
    private SellerProfile sellerProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType = DocumentType.GST_CERTIFICATE;

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private ApprovalStatus verificationStatus = ApprovalStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SellerDocument() {
    }

    public SellerDocument(SellerProfile sellerProfile, DocumentType documentType, String documentUrl, String documentNumber) {
        this.sellerProfile = sellerProfile;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.documentNumber = documentNumber;
        this.verificationStatus = ApprovalStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SellerProfile getSellerProfile() {
        return sellerProfile;
    }

    public void setSellerProfile(SellerProfile sellerProfile) {
        this.sellerProfile = sellerProfile;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public ApprovalStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(ApprovalStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Boolean getVerified() {
        return verificationStatus == ApprovalStatus.APPROVED;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
