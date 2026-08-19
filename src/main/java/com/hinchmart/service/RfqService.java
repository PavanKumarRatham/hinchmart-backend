package com.hinchmart.service;

import com.hinchmart.dto.request.RfqCreateRequest;
import com.hinchmart.dto.request.RfqItemRequest;
import com.hinchmart.dto.response.RfqDto;
import com.hinchmart.dto.response.RfqItemDto;
import com.hinchmart.entity.Product;
import com.hinchmart.entity.Rfq;
import com.hinchmart.entity.RfqItem;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.Role;
import com.hinchmart.entity.enums.RfqStatus;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.exception.UnauthorizedException;
import com.hinchmart.repository.ProductRepository;
import com.hinchmart.repository.RfqRepository;
import com.hinchmart.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RfqService {

    private final RfqRepository rfqRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    public RfqService(RfqRepository rfqRepository,
                      UserRepository userRepository,
                      ProductRepository productRepository,
                      ActivityLogService activityLogService) {
        this.rfqRepository = rfqRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public RfqDto createRfq(Long buyerUserId, RfqCreateRequest request) {
        User buyer = userRepository.findById(buyerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer user not found: " + buyerUserId));

        Rfq rfq = new Rfq();
        rfq.setRfqNumber("RFQ-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        rfq.setBuyer(buyer);
        rfq.setTitle(request.getTitle());
        rfq.setNotes(request.getNotes());
        rfq.setDeliveryPincode(request.getDeliveryPincode());
        rfq.setDeliveryCity(request.getDeliveryCity());
        rfq.setDeliveryLocation(request.getDeliveryLocation());
        rfq.setDeliveryTimelineDays(request.getDeliveryTimelineDays());
        rfq.setRequiredByDate(request.getRequiredByDate());
        rfq.setStatus(RfqStatus.OPEN);

        for (RfqItemRequest itemReq : request.getItems()) {
            Product product = null;
            if (itemReq.getProductId() != null) {
                product = productRepository.findById(itemReq.getProductId()).orElse(null);
            }

            RfqItem item = new RfqItem(
                    rfq,
                    product,
                    itemReq.getProductName(),
                    itemReq.getQuantity(),
                    itemReq.getUnit(),
                    itemReq.getTargetPrice(),
                    itemReq.getSpecifications()
            );
            rfq.addItem(item);
        }

        Rfq savedRfq = rfqRepository.save(rfq);

        activityLogService.log(buyerUserId, buyer.getEmail(), "RFQ_CREATED", "RFQ",
                savedRfq.getId(), "Created RFQ: " + savedRfq.getRfqNumber(), null);

        return mapToRfqDto(savedRfq);
    }

    @Transactional(readOnly = true)
    public List<RfqDto> getMyRfqs(Long buyerUserId) {
        return rfqRepository.findByBuyerIdOrderByCreatedAtDesc(buyerUserId).stream()
                .map(this::mapToRfqDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RfqDto getRfqById(Long id, Long currentUserId) {
        Rfq rfq = rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ not found with ID: " + id));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        // Buyer can only view their own RFQs unless user is SELLER, ADMIN, or SUPER_ADMIN
        if (currentUser.getRole() == Role.BUYER && !rfq.getBuyer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to view this RFQ");
        }

        return mapToRfqDto(rfq);
    }

    @Transactional(readOnly = true)
    public Page<RfqDto> getAllRfqs(Pageable pageable) {
        Page<Rfq> rfqs = rfqRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<RfqDto> dtos = rfqs.getContent().stream()
                .map(this::mapToRfqDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, rfqs.getTotalElements());
    }

    public RfqDto mapToRfqDto(Rfq rfq) {
        RfqDto dto = new RfqDto();
        dto.setId(rfq.getId());
        dto.setRfqNumber(rfq.getRfqNumber());
        dto.setTitle(rfq.getTitle());
        dto.setNotes(rfq.getNotes());
        dto.setDeliveryPincode(rfq.getDeliveryPincode());
        dto.setDeliveryCity(rfq.getDeliveryCity());
        dto.setDeliveryLocation(rfq.getDeliveryLocation());
        dto.setDeliveryTimelineDays(rfq.getDeliveryTimelineDays());
        dto.setRequiredByDate(rfq.getRequiredByDate());
        dto.setStatus(rfq.getStatus());
        dto.setCreatedAt(rfq.getCreatedAt());
        dto.setUpdatedAt(rfq.getUpdatedAt());

        if (rfq.getBuyer() != null) {
            dto.setBuyerId(rfq.getBuyer().getId());
            dto.setBuyerName(rfq.getBuyer().getFullName());
            dto.setBuyerEmail(rfq.getBuyer().getEmail());
            dto.setBuyerPhone(rfq.getBuyer().getPhone());
            if (rfq.getBuyer().getBuyerProfile() != null) {
                dto.setBuyerCompanyName(rfq.getBuyer().getBuyerProfile().getCompanyName());
            }
        }

        if (rfq.getItems() != null) {
            List<RfqItemDto> itemDtos = rfq.getItems().stream().map(item -> {
                RfqItemDto iDto = new RfqItemDto();
                iDto.setId(item.getId());
                if (item.getProduct() != null) {
                    iDto.setProductId(item.getProduct().getId());
                }
                iDto.setProductName(item.getProductName());
                iDto.setQuantity(item.getQuantity());
                iDto.setUnit(item.getUnit());
                iDto.setTargetPrice(item.getTargetPrice());
                iDto.setSpecifications(item.getSpecifications());
                return iDto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }
}
