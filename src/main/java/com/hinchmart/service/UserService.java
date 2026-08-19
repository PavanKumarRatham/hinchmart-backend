package com.hinchmart.service;

import com.hinchmart.dto.request.BuyerProfileUpdateRequest;
import com.hinchmart.dto.request.SellerProfileUpdateRequest;
import com.hinchmart.dto.request.SellerStatusUpdateRequest;
import com.hinchmart.dto.response.BuyerProfileDto;
import com.hinchmart.dto.response.DashboardStatsDto;
import com.hinchmart.dto.response.SellerDocumentDto;
import com.hinchmart.dto.response.SellerProfileDto;
import com.hinchmart.dto.response.UserDto;
import com.hinchmart.entity.BuyerProfile;
import com.hinchmart.entity.SellerProfile;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.ApprovalStatus;
import com.hinchmart.entity.enums.RfqStatus;
import com.hinchmart.entity.enums.Role;
import com.hinchmart.entity.enums.SellerStatus;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final ProductRepository productRepository;
    private final RfqRepository rfqRepository;
    private final CategoryRepository categoryRepository;
    private final AuthService authService;
    private final ActivityLogService activityLogService;

    public UserService(UserRepository userRepository,
                       BuyerProfileRepository buyerProfileRepository,
                       SellerProfileRepository sellerProfileRepository,
                       ProductRepository productRepository,
                       RfqRepository rfqRepository,
                       CategoryRepository categoryRepository,
                       AuthService authService,
                       ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.productRepository = productRepository;
        this.rfqRepository = rfqRepository;
        this.categoryRepository = categoryRepository;
        this.authService = authService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        long totalBuyers = userRepository.countByRole(Role.BUYER);
        long totalSellers = userRepository.countByRole(Role.SELLER);
        long pendingSellers = sellerProfileRepository.countByStatus(SellerStatus.PENDING) +
                sellerProfileRepository.countByStatus(SellerStatus.UNDER_REVIEW);
        long activeProducts = productRepository.countByApprovalStatusAndIsActiveTrue(ApprovalStatus.APPROVED);
        long openRfqs = rfqRepository.countByStatus(RfqStatus.OPEN);
        long totalRfqs = rfqRepository.count();
        long totalCategories = categoryRepository.count();

        return new DashboardStatsDto(
                totalBuyers,
                totalSellers,
                pendingSellers,
                activeProducts,
                openRfqs,
                totalRfqs,
                totalCategories
        );
    }

    @Transactional(readOnly = true)
    public BuyerProfileDto getBuyerProfile(Long userId) {
        BuyerProfile profile = buyerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer profile not found for user ID: " + userId));
        return mapToBuyerProfileDto(profile);
    }

    @Transactional
    public BuyerProfileDto updateBuyerProfile(Long userId, BuyerProfileUpdateRequest request) {
        BuyerProfile profile = buyerProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                    BuyerProfile newProfile = new BuyerProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
        if (request.getGstin() != null) profile.setGstin(request.getGstin());
        if (request.getBusinessType() != null) profile.setBusinessType(request.getBusinessType());
        if (request.getBillingAddress() != null) profile.setBillingAddress(request.getBillingAddress());
        if (request.getShippingAddress() != null) profile.setShippingAddress(request.getShippingAddress());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getPincode() != null) profile.setPincode(request.getPincode());
        if (request.getAnnualTurnover() != null) profile.setAnnualTurnover(request.getAnnualTurnover());

        BuyerProfile saved = buyerProfileRepository.save(profile);
        activityLogService.log(userId, null, "BUYER_PROFILE_UPDATED", "BUYER_PROFILE", saved.getId(), "Updated buyer profile", null);
        return mapToBuyerProfileDto(saved);
    }

    @Transactional(readOnly = true)
    public SellerProfileDto getSellerProfile(Long userId) {
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user ID: " + userId));
        return mapToSellerProfileDto(profile);
    }

    @Transactional(readOnly = true)
    public SellerProfileDto getSellerProfileById(Long sellerProfileId) {
        SellerProfile profile = sellerProfileRepository.findById(sellerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found with ID: " + sellerProfileId));
        return mapToSellerProfileDto(profile);
    }

    @Transactional
    public SellerProfileDto updateSellerProfile(Long userId, SellerProfileUpdateRequest request) {
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                    SellerProfile newProfile = new SellerProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
        if (request.getGstin() != null) profile.setGstin(request.getGstin());
        if (request.getPanNumber() != null) profile.setPanNumber(request.getPanNumber());
        if (request.getBusinessType() != null) profile.setBusinessType(request.getBusinessType());
        if (request.getWarehouseAddress() != null) profile.setWarehouseAddress(request.getWarehouseAddress());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getPincode() != null) profile.setPincode(request.getPincode());

        SellerProfile saved = sellerProfileRepository.save(profile);
        activityLogService.log(userId, null, "SELLER_PROFILE_UPDATED", "SELLER_PROFILE", saved.getId(), "Updated seller profile", null);
        return mapToSellerProfileDto(saved);
    }

    @Transactional
    public SellerProfileDto updateSellerStatus(Long sellerProfileId, SellerStatusUpdateRequest request) {
        SellerProfile profile = sellerProfileRepository.findById(sellerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found: " + sellerProfileId));

        profile.setStatus(request.getStatus());
        if (request.getStatus() == SellerStatus.APPROVED) {
            profile.setVerifiedAt(LocalDateTime.now());
            profile.setRejectionReason(null);
        } else if (request.getStatus() == SellerStatus.REJECTED || request.getStatus() == SellerStatus.UNDER_REVIEW) {
            profile.setRejectionReason(request.getRejectionReason());
        }

        SellerProfile saved = sellerProfileRepository.save(profile);
        activityLogService.log(profile.getUser() != null ? profile.getUser().getId() : null, null,
                "SELLER_STATUS_CHANGED", "SELLER_PROFILE", saved.getId(),
                "Status changed to " + request.getStatus().name(), null);
        return mapToSellerProfileDto(saved);
    }

    @Transactional
    public SellerProfileDto approveSeller(Long sellerProfileId) {
        SellerStatusUpdateRequest req = new SellerStatusUpdateRequest();
        req.setStatus(SellerStatus.APPROVED);
        return updateSellerStatus(sellerProfileId, req);
    }

    @Transactional
    public SellerProfileDto rejectSeller(Long sellerProfileId, String rejectionReason) {
        SellerStatusUpdateRequest req = new SellerStatusUpdateRequest();
        req.setStatus(SellerStatus.REJECTED);
        req.setRejectionReason(rejectionReason);
        return updateSellerStatus(sellerProfileId, req);
    }

    @Transactional
    public SellerProfileDto requestChangesSeller(Long sellerProfileId, String notes) {
        SellerStatusUpdateRequest req = new SellerStatusUpdateRequest();
        req.setStatus(SellerStatus.UNDER_REVIEW);
        req.setRejectionReason(notes);
        return updateSellerStatus(sellerProfileId, req);
    }

    @Transactional
    public SellerProfileDto suspendSeller(Long sellerProfileId) {
        SellerStatusUpdateRequest req = new SellerStatusUpdateRequest();
        req.setStatus(SellerStatus.SUSPENDED);
        return updateSellerStatus(sellerProfileId, req);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SellerProfileDto> getAllSellers(SellerStatus status) {
        List<SellerProfile> sellers = (status != null) ?
                sellerProfileRepository.findByStatus(status) :
                sellerProfileRepository.findAll();

        return sellers.stream()
                .map(this::mapToSellerProfileDto)
                .collect(Collectors.toList());
    }

    public BuyerProfileDto mapToBuyerProfileDto(BuyerProfile bp) {
        BuyerProfileDto dto = new BuyerProfileDto();
        dto.setId(bp.getId());
        dto.setCompanyName(bp.getCompanyName());
        dto.setGstin(bp.getGstin());
        dto.setBusinessType(bp.getBusinessType());
        dto.setBillingAddress(bp.getBillingAddress());
        dto.setShippingAddress(bp.getShippingAddress());
        dto.setCity(bp.getCity());
        dto.setState(bp.getState());
        dto.setPincode(bp.getPincode());
        dto.setCreditLimit(bp.getCreditLimit());
        dto.setAnnualTurnover(bp.getAnnualTurnover());
        return dto;
    }

    public SellerProfileDto mapToSellerProfileDto(SellerProfile sp) {
        SellerProfileDto dto = new SellerProfileDto();
        dto.setId(sp.getId());
        dto.setCompanyName(sp.getCompanyName());
        dto.setGstin(sp.getGstin());
        dto.setPanNumber(sp.getPanNumber());
        dto.setBusinessType(sp.getBusinessType());
        dto.setWarehouseAddress(sp.getWarehouseAddress());
        dto.setCity(sp.getCity());
        dto.setState(sp.getState());
        dto.setPincode(sp.getPincode());
        dto.setRating(sp.getRating());
        dto.setStatus(sp.getStatus());
        dto.setBankAccountNumber(sp.getBankAccountNumber());
        dto.setBankIfscCode(sp.getBankIfscCode());
        dto.setBankName(sp.getBankName());
        dto.setBankAccountName(sp.getBankAccountName());
        dto.setRejectionReason(sp.getRejectionReason());
        dto.setVerifiedAt(sp.getVerifiedAt());
        dto.setCreatedAt(sp.getCreatedAt());

        if (sp.getUser() != null) {
            dto.setUserId(sp.getUser().getId());
            dto.setContactPerson(sp.getUser().getFullName());
            dto.setPhone(sp.getUser().getPhone());
            dto.setEmail(sp.getUser().getEmail());
        }

        if (sp.getDocuments() != null && !sp.getDocuments().isEmpty()) {
            List<SellerDocumentDto> docs = sp.getDocuments().stream().map(doc -> {
                SellerDocumentDto dDto = new SellerDocumentDto();
                dDto.setId(doc.getId());
                dDto.setDocumentType(doc.getDocumentType());
                dDto.setDocumentUrl(doc.getDocumentUrl());
                dDto.setDocumentNumber(doc.getDocumentNumber());
                dDto.setVerified(doc.getVerified());
                dDto.setVerifiedAt(doc.getVerifiedAt());
                return dDto;
            }).collect(Collectors.toList());
            dto.setDocuments(docs);
        }

        return dto;
    }
}
