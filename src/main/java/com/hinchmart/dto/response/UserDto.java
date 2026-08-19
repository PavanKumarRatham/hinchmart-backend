package com.hinchmart.dto.response;

import com.hinchmart.entity.enums.AccountStatus;
import com.hinchmart.entity.enums.Role;
import java.time.LocalDateTime;

public class UserDto {

    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private Role role;
    private AccountStatus status;
    private BuyerProfileDto buyerProfile;
    private SellerProfileDto sellerProfile;
    private LocalDateTime createdAt;

    public UserDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public BuyerProfileDto getBuyerProfile() {
        return buyerProfile;
    }

    public void setBuyerProfile(BuyerProfileDto buyerProfile) {
        this.buyerProfile = buyerProfile;
    }

    public SellerProfileDto getSellerProfile() {
        return sellerProfile;
    }

    public void setSellerProfile(SellerProfileDto sellerProfile) {
        this.sellerProfile = sellerProfile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
