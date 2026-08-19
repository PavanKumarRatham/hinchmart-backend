package com.hinchmart.service;

import com.hinchmart.dto.request.SellerStoreRequest;
import com.hinchmart.dto.response.OrderDto;
import com.hinchmart.dto.response.SellerDashboardDto;
import com.hinchmart.dto.response.SellerStoreDto;
import com.hinchmart.entity.Order;
import com.hinchmart.entity.SellerProfile;
import com.hinchmart.entity.SellerStore;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.*;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellerService {

    private final SellerStoreRepository sellerStoreRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final RfqRepository rfqRepository;
    private final ActivityLogService activityLogService;

    public SellerService(SellerStoreRepository sellerStoreRepository,
                         SellerProfileRepository sellerProfileRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository,
                         RfqRepository rfqRepository,
                         ActivityLogService activityLogService) {
        this.sellerStoreRepository = sellerStoreRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.rfqRepository = rfqRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public SellerStoreDto createStore(Long sellerUserId, SellerStoreRequest request) {
        User seller = userRepository.findById(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller user not found with ID: " + sellerUserId));

        // Verify seller status is APPROVED
        SellerProfile sellerProfile = seller.getSellerProfile();
        if (sellerProfile == null || sellerProfile.getStatus() != SellerStatus.APPROVED) {
            throw new BadRequestException("Seller verification is pending");
        }

        if (sellerStoreRepository.existsBySellerId(sellerUserId)) {
            throw new BadRequestException("Store already exists for this seller. Please use update instead.");
        }

        String slug = (request.getStoreSlug() != null && !request.getStoreSlug().trim().isEmpty()) ?
                request.getStoreSlug().toLowerCase().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-") :
                generateUniqueSlug(request.getStoreName());

        SellerStore store = new SellerStore(
                seller,
                request.getStoreName(),
                slug,
                request.getLogo(),
                request.getBanner(),
                request.getDescription(),
                request.getBusinessEmail() != null ? request.getBusinessEmail() : seller.getEmail(),
                request.getBusinessMobile() != null ? request.getBusinessMobile() : seller.getPhone(),
                request.getGstin() != null ? request.getGstin() : (sellerProfile.getGstin()),
                request.getAddress() != null ? request.getAddress() : (sellerProfile.getWarehouseAddress()),
                StoreStatus.ACTIVE
        );

        SellerStore saved = sellerStoreRepository.save(store);

        activityLogService.log(sellerUserId, seller.getEmail(), "STORE_CREATED", "STORE",
                saved.getId(), "Created seller store: " + saved.getStoreName(), null);

        return mapToStoreDto(saved);
    }

    @Transactional(readOnly = true)
    public SellerStoreDto getStore(Long sellerUserId) {
        SellerStore store = sellerStoreRepository.findBySellerId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No store found for this seller. Please create one."));
        return mapToStoreDto(store);
    }

    @Transactional(readOnly = true)
    public SellerStoreDto getStoreBySlug(String storeSlug) {
        SellerStore store = sellerStoreRepository.findByStoreSlug(storeSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with slug: " + storeSlug));
        return mapToStoreDto(store);
    }

    @Transactional
    public SellerStoreDto updateStore(Long sellerUserId, SellerStoreRequest request) {
        SellerStore store = sellerStoreRepository.findBySellerId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No store found for this seller. Please create one first."));

        if (request.getStoreName() != null) store.setStoreName(request.getStoreName());
        if (request.getLogo() != null) store.setLogo(request.getLogo());
        if (request.getBanner() != null) store.setBanner(request.getBanner());
        if (request.getDescription() != null) store.setDescription(request.getDescription());
        if (request.getBusinessEmail() != null) store.setBusinessEmail(request.getBusinessEmail());
        if (request.getBusinessMobile() != null) store.setBusinessMobile(request.getBusinessMobile());
        if (request.getGstin() != null) store.setGstin(request.getGstin());
        if (request.getAddress() != null) store.setAddress(request.getAddress());

        SellerStore updated = sellerStoreRepository.save(store);
        return mapToStoreDto(updated);
    }

    @Transactional(readOnly = true)
    public SellerDashboardDto getSellerDashboard(Long sellerUserId) {
        long totalProducts = productRepository.countBySellerId(sellerUserId);
        long activeProducts = productRepository.countBySellerIdAndApprovalStatusAndIsActiveTrue(sellerUserId, ApprovalStatus.APPROVED);
        long pendingProducts = productRepository.countBySellerIdAndApprovalStatus(sellerUserId, ApprovalStatus.PENDING);
        long lowStockProducts = productRepository.countBySellerIdAndStockLessThanEqual(sellerUserId, 5);

        long totalOrders = orderRepository.countOrdersBySellerId(sellerUserId);
        long newOrders = orderRepository.countOrdersBySellerIdAndStatus(sellerUserId, OrderStatus.PLACED) +
                         orderRepository.countOrdersBySellerIdAndStatus(sellerUserId, OrderStatus.CONFIRMED);

        long openRfqs = rfqRepository.countByStatus(RfqStatus.OPEN);
        BigDecimal revenue = orderRepository.calculateSellerRevenue(sellerUserId);

        List<Order> recentOrdersList = orderRepository.findTop5OrdersBySellerId(sellerUserId, PageRequest.of(0, 5));
        List<OrderDto> recentOrderDtos = recentOrdersList.stream()
                .map(this::mapToOrderSummaryDto)
                .collect(Collectors.toList());

        return new SellerDashboardDto(
                totalProducts,
                activeProducts,
                pendingProducts,
                totalOrders,
                newOrders,
                openRfqs,
                revenue != null ? revenue : BigDecimal.ZERO,
                lowStockProducts,
                recentOrderDtos
        );
    }

    public SellerStoreDto mapToStoreDto(SellerStore store) {
        SellerStoreDto dto = new SellerStoreDto();
        dto.setId(store.getId());
        dto.setStoreName(store.getStoreName());
        dto.setStoreSlug(store.getStoreSlug());
        dto.setLogo(store.getLogo());
        dto.setBanner(store.getBanner());
        dto.setDescription(store.getDescription());
        dto.setBusinessEmail(store.getBusinessEmail());
        dto.setBusinessMobile(store.getBusinessMobile());
        dto.setGstin(store.getGstin());
        dto.setAddress(store.getAddress());
        dto.setStatus(store.getStatus());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());

        if (store.getSeller() != null) {
            dto.setSellerId(store.getSeller().getId());
            dto.setSellerName(store.getSeller().getFullName());
        }

        return dto;
    }

    private OrderDto mapToOrderSummaryDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setSubtotal(order.getSubtotal());
        dto.setGstAmount(order.getGstAmount());
        dto.setDeliveryCharge(order.getDeliveryCharge());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setCreatedAt(order.getCreatedAt());
        if (order.getBuyer() != null) {
            dto.setBuyerId(order.getBuyer().getId());
            dto.setBuyerName(order.getBuyer().getFullName());
        }
        return dto;
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
        String slug = baseSlug;
        int count = 1;
        while (sellerStoreRepository.findByStoreSlug(slug).isPresent()) {
            slug = baseSlug + "-" + count++;
        }
        return slug;
    }
}
