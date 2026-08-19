package com.hinchmart.service;

import com.hinchmart.dto.request.CreateShipmentRequest;
import com.hinchmart.dto.request.UpdateShipmentStatusRequest;
import com.hinchmart.dto.response.ShipmentDto;
import com.hinchmart.dto.response.ShipmentTrackingDto;
import com.hinchmart.entity.*;
import com.hinchmart.entity.enums.NotificationType;
import com.hinchmart.entity.enums.OrderStatus;
import com.hinchmart.entity.enums.Role;
import com.hinchmart.entity.enums.ShipmentStatus;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.exception.UnauthorizedException;
import com.hinchmart.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository shipmentTrackingRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           ShipmentTrackingRepository shipmentTrackingRepository,
                           DeliveryPartnerRepository deliveryPartnerRepository,
                           OrderRepository orderRepository,
                           UserRepository userRepository,
                           NotificationService notificationService,
                           ActivityLogService activityLogService) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentTrackingRepository = shipmentTrackingRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public ShipmentDto createShipment(Long orderId, Long sellerUserId, CreateShipmentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        User seller = userRepository.findById(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerUserId));

        // Verify seller or admin
        if (seller.getRole() != Role.ADMIN && seller.getRole() != Role.SUPER_ADMIN) {
            boolean isSellerOfOrder = order.getItems().stream()
                    .anyMatch(item -> item.getSeller() != null && item.getSeller().getId().equals(sellerUserId));
            if (!isSellerOfOrder) {
                throw new UnauthorizedException("You do not have permission to create a shipment for this order");
            }
        }

        Optional<Shipment> existingOpt = shipmentRepository.findByOrderId(orderId);
        if (existingOpt.isPresent()) {
            throw new BadRequestException("Shipment already exists for order " + order.getOrderNumber());
        }

        DeliveryPartner deliveryPartner = null;
        if (request.getDeliveryPartnerId() != null) {
            deliveryPartner = deliveryPartnerRepository.findById(request.getDeliveryPartnerId()).orElse(null);
        } else if (request.getDeliveryPartnerCode() != null) {
            deliveryPartner = deliveryPartnerRepository.findByCode(request.getDeliveryPartnerCode()).orElse(null);
        }

        if (deliveryPartner == null) {
            // Default delivery partner
            List<DeliveryPartner> partners = deliveryPartnerRepository.findByIsActiveTrueOrderByNameAsc();
            deliveryPartner = partners.isEmpty() ? null : partners.get(0);
        }

        String shipmentNumber = "SHP-2026-" + System.currentTimeMillis() % 10000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String trackingNumber = (request.getTrackingNumber() != null && !request.getTrackingNumber().trim().isEmpty()) ?
                request.getTrackingNumber() :
                "TRK-" + System.currentTimeMillis() % 10000000 + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String awbCode = (request.getAwbCode() != null) ? request.getAwbCode() : "AWB-" + trackingNumber;

        Shipment shipment = new Shipment();
        shipment.setShipmentNumber(shipmentNumber);
        shipment.setOrder(order);
        shipment.setSeller(seller);
        shipment.setDeliveryPartner(deliveryPartner);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setAwbCode(awbCode);
        shipment.setShippingLabelUrl(request.getShippingLabelUrl());
        shipment.setStatus(ShipmentStatus.PICKUP_SCHEDULED);
        shipment.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate() != null ? request.getEstimatedDeliveryDate() : LocalDate.now().plusDays(3));
        shipment.setShippingAddress(order.getShippingAddress());
        shipment.setNotes(request.getNotes());

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Initial tracking checkpoint
        ShipmentTracking checkpoint = new ShipmentTracking(
                savedShipment,
                ShipmentStatus.PICKUP_SCHEDULED,
                "Origin Logistics Yard",
                "Shipment created by seller. Carrier pickup scheduled."
        );
        shipmentTrackingRepository.save(checkpoint);
        savedShipment.addCheckpoint(checkpoint);

        // Update Order status
        order.setOrderStatus(OrderStatus.READY_TO_SHIP);
        order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.READY_TO_SHIP, "Shipment booked with tracking " + trackingNumber, seller));
        orderRepository.save(order);

        // Notify Buyer
        notificationService.sendNotification(
                order.getBuyer(),
                "Order Packed & Ready to Ship!",
                "Your order " + order.getOrderNumber() + " has been packed. Tracking Number: " + trackingNumber,
                NotificationType.ORDER_SHIPPED,
                order.getId(),
                "ORDER"
        );

        activityLogService.log(sellerUserId, seller.getEmail(), "SHIPMENT_CREATED", "SHIPMENT",
                savedShipment.getId(), "Created shipment " + shipmentNumber + " for order " + order.getOrderNumber(), null);

        return mapToShipmentDto(savedShipment);
    }

    @Transactional(readOnly = true)
    public ShipmentDto getTrackingByOrderId(Long orderId, Long currentUserId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No shipment found for order ID: " + orderId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (currentUser.getRole() == Role.BUYER && !shipment.getOrder().getBuyer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to view this shipment");
        }

        return mapToShipmentDto(shipment);
    }

    @Transactional
    public ShipmentDto updateShipmentStatus(Long shipmentId, Long currentUserId, UpdateShipmentStatusRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with ID: " + shipmentId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        shipment.setStatus(request.getStatus());

        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
        }

        // Add tracking checkpoint
        String location = request.getLocation() != null ? request.getLocation() : "Logistics Hub";
        String description = request.getDescription() != null ? request.getDescription() : "Status updated to " + request.getStatus().name();

        ShipmentTracking checkpoint = new ShipmentTracking(shipment, request.getStatus(), location, description);
        shipmentTrackingRepository.save(checkpoint);
        shipment.addCheckpoint(checkpoint);

        Order order = shipment.getOrder();

        // Sync order status
        if (request.getStatus() == ShipmentStatus.PICKED_UP || request.getStatus() == ShipmentStatus.IN_TRANSIT) {
            order.setOrderStatus(OrderStatus.SHIPPED);
            order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.SHIPPED, "In transit via carrier. Location: " + location, currentUser));
            notificationService.sendNotification(
                    order.getBuyer(),
                    "Order Shipped & In Transit!",
                    "Your order " + order.getOrderNumber() + " is currently in transit (" + location + ").",
                    NotificationType.ORDER_SHIPPED,
                    order.getId(),
                    "ORDER"
            );
        } else if (request.getStatus() == ShipmentStatus.OUT_FOR_DELIVERY) {
            order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
            order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.OUT_FOR_DELIVERY, "Out for delivery with delivery team", currentUser));
            notificationService.sendNotification(
                    order.getBuyer(),
                    "Out for Delivery Today!",
                    "Your order " + order.getOrderNumber() + " is out for delivery.",
                    NotificationType.OUT_FOR_DELIVERY,
                    order.getId(),
                    "ORDER"
            );
        } else if (request.getStatus() == ShipmentStatus.DELIVERED) {
            order.setOrderStatus(OrderStatus.DELIVERED);
            order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.DELIVERED, "Delivered to buyer destination", currentUser));
            notificationService.sendNotification(
                    order.getBuyer(),
                    "Order Delivered Successfully!",
                    "Your order " + order.getOrderNumber() + " has been successfully delivered.",
                    NotificationType.ORDER_DELIVERED,
                    order.getId(),
                    "ORDER"
            );
        }
        orderRepository.save(order);

        Shipment saved = shipmentRepository.save(shipment);
        return mapToShipmentDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentDto> getAllShipments(ShipmentStatus status, Pageable pageable) {
        Page<Shipment> shipments = (status != null) ?
                shipmentRepository.findByStatusOrderByCreatedAtDesc(status, pageable) :
                shipmentRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<ShipmentDto> dtos = shipments.getContent().stream()
                .map(this::mapToShipmentDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, shipments.getTotalElements());
    }

    public ShipmentDto mapToShipmentDto(Shipment s) {
        ShipmentDto dto = new ShipmentDto();
        dto.setId(s.getId());
        dto.setShipmentNumber(s.getShipmentNumber());
        if (s.getOrder() != null) {
            dto.setOrderId(s.getOrder().getId());
            dto.setOrderNumber(s.getOrder().getOrderNumber());
        }
        if (s.getSeller() != null) {
            dto.setSellerId(s.getSeller().getId());
            dto.setSellerName(s.getSeller().getFullName());
        }
        if (s.getDeliveryPartner() != null) {
            dto.setDeliveryPartnerId(s.getDeliveryPartner().getId());
            dto.setDeliveryPartnerName(s.getDeliveryPartner().getName());
            dto.setDeliveryPartnerCode(s.getDeliveryPartner().getCode());
            if (s.getDeliveryPartner().getTrackingUrlTemplate() != null && s.getTrackingNumber() != null) {
                dto.setTrackingUrl(s.getDeliveryPartner().getTrackingUrlTemplate().replace("{trackingNumber}", s.getTrackingNumber()));
            }
        }
        dto.setTrackingNumber(s.getTrackingNumber());
        dto.setAwbCode(s.getAwbCode());
        dto.setShippingLabelUrl(s.getShippingLabelUrl());
        dto.setStatus(s.getStatus());
        dto.setEstimatedDeliveryDate(s.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(s.getActualDeliveryDate());
        dto.setShippingAddress(s.getShippingAddress());
        dto.setNotes(s.getNotes());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());

        if (s.getTrackingCheckpoints() != null) {
            List<ShipmentTrackingDto> checkpoints = s.getTrackingCheckpoints().stream().map(c -> new ShipmentTrackingDto(
                    c.getId(),
                    s.getId(),
                    c.getStatus(),
                    c.getLocation(),
                    c.getDescription(),
                    c.getTimestamp()
            )).collect(Collectors.toList());
            dto.setCheckpoints(checkpoints);
        }

        return dto;
    }
}
