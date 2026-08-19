package com.hinchmart.dto.response;

import com.hinchmart.entity.enums.DocumentType;
import java.time.LocalDateTime;

public class SellerDocumentDto {

    private Long id;
    private DocumentType documentType;
    private String documentUrl;
    private String documentNumber;
    private Boolean verified;
    private LocalDateTime verifiedAt;

    public SellerDocumentDto() {
    }

    public SellerDocumentDto(Long id, DocumentType documentType, String documentUrl, String documentNumber, Boolean verified, LocalDateTime verifiedAt) {
        this.id = id;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.documentNumber = documentNumber;
        this.verified = verified;
        this.verifiedAt = verifiedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
