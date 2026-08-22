package com.example.hinchmart.service;

import com.example.hinchmart.dto.*;
import com.example.hinchmart.entity.*;
import com.example.hinchmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           ShipmentTrackingRepository trackingRepository,
                           DeliveryPartnerRepository partnerRepository,
                           OrderRepository orderRepository,
                           NotificationService notificationService) {
        this.shipmentRepository = shipmentRepository;
        this.trackingRepository = trackingRepository;
        this.partnerRepository = partnerRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    /**
     * POST /api/seller/orders/{id}/shipment
     * Create a shipment for an order
     */
    @Transactional
    public ShipmentResponse createShipment(Long orderId, CreateShipmentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        DeliveryPartner partner = null;
        if (request.getDeliveryPartnerId() != null) {
            partner = partnerRepository.findById(request.getDeliveryPartnerId())
                    .orElse(null);
        }
        if (partner == null && request.getDeliveryPartnerCode() != null) {
            partner = partnerRepository.findByPartnerCode(request.getDeliveryPartnerCode().toUpperCase())
                    .orElse(null);
        }

        // Determine sellerId
        Long sellerId = request.getSellerId();
        if (sellerId == null && !order.getItems().isEmpty()) {
            sellerId = order.getItems().get(0).getSellerId();
        }
        if (sellerId == null) {
            sellerId = 101L; // Fallback default
        }

        String trackingNumber = request.getTrackingNumber();
        if (trackingNumber == null || trackingNumber.isBlank()) {
            String partnerPrefix = (partner != null && partner.getPartnerCode() != null) ? partner.getPartnerCode() : "HINCH";
            trackingNumber = "TRK-" + partnerPrefix + "-" + (100000 + new Random().nextInt(900000));
        }

        Shipment shipment = new Shipment();
        shipment.setShipmentNumber(generateShipmentNumber());
        shipment.setOrder(order);
        shipment.setSellerId(sellerId);
        shipment.setDeliveryPartner(partner);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setShippingMode(request.getShippingMode() != null ? request.getShippingMode() : "SURFACE_BULK");
        shipment.setPackageWeightKg(request.getPackageWeightKg() != null ? request.getPackageWeightKg() : 50.0);
        shipment.setPackageDimensions(request.getPackageDimensions() != null ? request.getPackageDimensions() : "Standard Pallet");
        shipment.setTotalPackages(request.getTotalPackages() != null ? request.getTotalPackages() : 1);
        shipment.setStatus("PICKUP_SCHEDULED");
        shipment.setPickupAddress(request.getPickupAddress() != null ? request.getPickupAddress() : "Seller Central Warehouse");
        shipment.setDeliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : order.getShippingAddress());
        shipment.setPickupScheduledAt(request.getPickupScheduledAt() != null ? request.getPickupScheduledAt() : LocalDateTime.now().plusHours(4));
        shipment.setEstimatedDeliveryAt(request.getEstimatedDeliveryAt() != null ? request.getEstimatedDeliveryAt() : LocalDateTime.now().plusDays(3));
        shipment.setDriverName(request.getDriverName());
        shipment.setDriverContact(request.getDriverContact());
        shipment.setVehicleNumber(request.getVehicleNumber());
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Initial tracking history checkpoint
        ShipmentTracking initialTracking = new ShipmentTracking();
        initialTracking.setShipment(savedShipment);
        initialTracking.setStatus("PICKUP_SCHEDULED");
        initialTracking.setLocation(savedShipment.getPickupAddress());
        initialTracking.setDescription(request.getInitialRemarks() != null ? request.getInitialRemarks() : "Pickup scheduled with logistics partner");
        initialTracking.setRecordedBy("SELLER");
        initialTracking.setEventTime(LocalDateTime.now());
        trackingRepository.save(initialTracking);

        savedShipment.getTrackingHistory().add(initialTracking);

        // Update Order status to READY_TO_SHIP if currently PLACED / CONFIRMED
        if ("PLACED".equalsIgnoreCase(order.getOrderStatus()) || "CONFIRMED".equalsIgnoreCase(order.getOrderStatus())) {
            order.setOrderStatus("READY_TO_SHIP");
            order.setUpdatedAt(LocalDateTime.now());

            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setStatus("READY_TO_SHIP");
            history.setChangedBy("SELLER");
            history.setRemarks("Shipment " + savedShipment.getShipmentNumber() + " created. Ready for carrier pickup.");
            history.setCreatedAt(LocalDateTime.now());
            order.getStatusHistory().add(history);
            orderRepository.save(order);
        }

        // Automatic notification to Buyer
        String partnerName = partner != null ? partner.getPartnerName() : "Logistics Partner";
        notificationService.sendNotification(
                order.getBuyerId(),
                "BUYER",
                "ORDER_SHIPPED",
                "Shipment Scheduled: " + savedShipment.getShipmentNumber(),
                "Your order #" + order.getOrderNumber() + " shipment has been scheduled with " + partnerName + ". Tracking No: " + trackingNumber,
                "SHIPMENT",
                savedShipment.getId()
        );

        return mapToShipmentResponse(savedShipment);
    }

    /**
     * PATCH /api/seller/shipments/{id}/status
     * Update shipment status and register tracking history
     */
    @Transactional
    public ShipmentResponse updateShipmentStatus(Long shipmentId, ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found with id: " + shipmentId));

        String newStatus = request.getStatus().trim().toUpperCase();
        shipment.setStatus(newStatus);
        shipment.setUpdatedAt(LocalDateTime.now());

        if ("DELIVERED".equalsIgnoreCase(newStatus)) {
            shipment.setActualDeliveryAt(LocalDateTime.now());
        }

        // Add tracking checkpoint
        ShipmentTracking tracking = new ShipmentTracking();
        tracking.setShipment(shipment);
        tracking.setStatus(newStatus);
        tracking.setLocation(request.getLocation() != null ? request.getLocation() : "Logistics Hub");
        tracking.setDescription(request.getDescription() != null ? request.getDescription() : "Shipment status updated to " + newStatus);
        tracking.setLatitude(request.getLatitude());
        tracking.setLongitude(request.getLongitude());
        tracking.setRecordedBy(request.getUpdatedBy() != null ? request.getUpdatedBy().toUpperCase() : "DELIVERY_PARTNER");
        tracking.setEventTime(LocalDateTime.now());

        trackingRepository.save(tracking);
        shipment.getTrackingHistory().add(tracking);

        Shipment updatedShipment = shipmentRepository.save(shipment);
        Order order = shipment.getOrder();

        // Synchronize Order status based on shipment milestone and trigger automatic notifications
        syncOrderStatusAndNotify(order, shipment, newStatus, request.getDescription());

        return mapToShipmentResponse(updatedShipment);
    }

    /**
     * GET /api/orders/{id}/tracking
     * Consolidated order tracking timeline and shipment details
     */
    @Transactional(readOnly = true)
    public OrderTrackingResponse getOrderTracking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);

        OrderTrackingResponse res = new OrderTrackingResponse();
        res.setOrderId(order.getId());
        res.setOrderNumber(order.getOrderNumber());
        res.setOrderStatus(order.getOrderStatus());
        res.setBuyerId(order.getBuyerId());
        res.setShippingAddress(order.getShippingAddress());
        res.setTotalAmount(order.getTotalAmount());
        res.setOrderDate(order.getCreatedAt());

        List<ShipmentResponse> shipmentResponses = shipments.stream()
                .map(this::mapToShipmentResponse)
                .collect(Collectors.toList());
        res.setAllShipments(shipmentResponses);

        if (!shipmentResponses.isEmpty()) {
            res.setActiveShipment(shipmentResponses.get(shipmentResponses.size() - 1));
        }

        List<OrderStatusHistoryResponse> historyResponses = order.getStatusHistory().stream()
                .map(h -> new OrderStatusHistoryResponse(h.getId(), h.getStatus(), h.getChangedBy(), h.getRemarks(), h.getCreatedAt()))
                .collect(Collectors.toList());
        res.setStatusHistory(historyResponses);

        return res;
    }

    /**
     * GET /api/admin/shipments
     * List all shipments with optional filtering
     */
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAdminShipments(String status, Long sellerId) {
        List<Shipment> list;
        if (sellerId != null && status != null && !status.isBlank()) {
            list = shipmentRepository.findBySellerIdAndStatus(sellerId, status.toUpperCase());
        } else if (sellerId != null) {
            list = shipmentRepository.findBySellerId(sellerId);
        } else if (status != null && !status.isBlank()) {
            list = shipmentRepository.findByStatus(status.toUpperCase());
        } else {
            list = shipmentRepository.findAll();
        }
        return list.stream().map(this::mapToShipmentResponse).collect(Collectors.toList());
    }

    /**
     * List available active delivery partners
     */
    @Transactional(readOnly = true)
    public List<DeliveryPartnerResponse> getDeliveryPartners() {
        return partnerRepository.findByActiveTrue().stream()
                .map(this::mapToPartnerResponse)
                .collect(Collectors.toList());
    }

    private void syncOrderStatusAndNotify(Order order, Shipment shipment, String shipmentStatus, String remarks) {
        if (order == null) return;

        switch (shipmentStatus) {
            case "PICKED_UP":
            case "IN_TRANSIT":
                if (!"SHIPPED".equalsIgnoreCase(order.getOrderStatus())) {
                    updateOrderState(order, "SHIPPED", "Carrier picked up shipment #" + shipment.getShipmentNumber());
                    notificationService.sendNotification(
                            order.getBuyerId(),
                            "BUYER",
                            "ORDER_SHIPPED",
                            "Order #" + order.getOrderNumber() + " Dispatched",
                            "Your order is now in transit. Tracking number: " + shipment.getTrackingNumber(),
                            "ORDER",
                            order.getId()
                    );
                }
                break;

            case "OUT_FOR_DELIVERY":
                updateOrderState(order, "OUT_FOR_DELIVERY", "Consignment is out for delivery with local driver");
                notificationService.sendNotification(
                        order.getBuyerId(),
                        "BUYER",
                        "OUT_FOR_DELIVERY",
                        "Out for Delivery: Order #" + order.getOrderNumber(),
                        "Your shipment #" + shipment.getShipmentNumber() + " is out for delivery today. Driver: "
                                + (shipment.getDriverName() != null ? shipment.getDriverName() : "Assigned Driver"),
                        "ORDER",
                        order.getId()
                );
                break;

            case "DELIVERED":
                updateOrderState(order, "DELIVERED", "Shipment successfully delivered to destination");
                notificationService.sendNotification(
                        order.getBuyerId(),
                        "BUYER",
                        "ORDER_DELIVERED",
                        "Order Delivered: #" + order.getOrderNumber(),
                        "Your order has been successfully delivered to " + shipment.getDeliveryAddress(),
                        "ORDER",
                        order.getId()
                );
                // Notify seller as well
                notificationService.sendNotification(
                        shipment.getSellerId(),
                        "SELLER",
                        "ORDER_DELIVERED",
                        "Delivery Confirmed: Order #" + order.getOrderNumber(),
                        "Shipment #" + shipment.getShipmentNumber() + " has been delivered to the customer.",
                        "SHIPMENT",
                        shipment.getId()
                );
                break;

            case "FAILED_DELIVERY":
                notificationService.sendNotification(
                        order.getBuyerId(),
                        "BUYER",
                        "ORDER_SHIPPED",
                        "Delivery Attempt Failed: Order #" + order.getOrderNumber(),
                        "Delivery could not be completed: " + (remarks != null ? remarks : "Customer unavailable. Re-attempt scheduled."),
                        "SHIPMENT",
                        shipment.getId()
                );
                break;

            case "RETURN_TO_ORIGIN":
                updateOrderState(order, "RETURNED", "Consignment returning to origin: " + remarks);
                notificationService.sendNotification(
                        shipment.getSellerId(),
                        "SELLER",
                        "ORDER_SHIPPED",
                        "Return Initiated: Shipment #" + shipment.getShipmentNumber(),
                        "Shipment is being returned to seller origin hub.",
                        "SHIPMENT",
                        shipment.getId()
                );
                break;

            default:
                break;
        }
    }

    private void updateOrderState(Order order, String newStatus, String remarks) {
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(newStatus);
        history.setChangedBy("DELIVERY_PARTNER");
        history.setRemarks(remarks);
        history.setCreatedAt(LocalDateTime.now());

        order.getStatusHistory().add(history);
        orderRepository.save(order);
    }

    private String generateShipmentNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomSuffix = 100 + new Random().nextInt(900);
        return "SHP-" + dateStr + "-" + randomSuffix;
    }

    public ShipmentResponse mapToShipmentResponse(Shipment shipment) {
        ShipmentResponse res = new ShipmentResponse();
        res.setId(shipment.getId());
        res.setShipmentNumber(shipment.getShipmentNumber());
        res.setOrderId(shipment.getOrder() != null ? shipment.getOrder().getId() : null);
        res.setOrderNumber(shipment.getOrder() != null ? shipment.getOrder().getOrderNumber() : null);
        res.setSellerId(shipment.getSellerId());

        if (shipment.getDeliveryPartner() != null) {
            res.setDeliveryPartner(mapToPartnerResponse(shipment.getDeliveryPartner()));
            if (shipment.getDeliveryPartner().getTrackingUrlTemplate() != null && shipment.getTrackingNumber() != null) {
                res.setTrackingUrl(shipment.getDeliveryPartner().getTrackingUrlTemplate().replace("{tracking_number}", shipment.getTrackingNumber()));
            }
        }

        res.setTrackingNumber(shipment.getTrackingNumber());
        res.setShippingMode(shipment.getShippingMode());
        res.setPackageWeightKg(shipment.getPackageWeightKg());
        res.setPackageDimensions(shipment.getPackageDimensions());
        res.setTotalPackages(shipment.getTotalPackages());
        res.setStatus(shipment.getStatus());
        res.setPickupAddress(shipment.getPickupAddress());
        res.setDeliveryAddress(shipment.getDeliveryAddress());
        res.setPickupScheduledAt(shipment.getPickupScheduledAt());
        res.setEstimatedDeliveryAt(shipment.getEstimatedDeliveryAt());
        res.setActualDeliveryAt(shipment.getActualDeliveryAt());
        res.setDriverName(shipment.getDriverName());
        res.setDriverContact(shipment.getDriverContact());
        res.setVehicleNumber(shipment.getVehicleNumber());
        res.setCreatedAt(shipment.getCreatedAt());
        res.setUpdatedAt(shipment.getUpdatedAt());

        if (shipment.getTrackingHistory() != null) {
            List<ShipmentTrackingResponse> trackingResponses = shipment.getTrackingHistory().stream().map(t -> {
                ShipmentTrackingResponse tr = new ShipmentTrackingResponse();
                tr.setId(t.getId());
                tr.setShipmentId(shipment.getId());
                tr.setStatus(t.getStatus());
                tr.setLocation(t.getLocation());
                tr.setDescription(t.getDescription());
                tr.setLatitude(t.getLatitude());
                tr.setLongitude(t.getLongitude());
                tr.setEventTime(t.getEventTime());
                tr.setRecordedBy(t.getRecordedBy());
                return tr;
            }).collect(Collectors.toList());
            res.setTrackingHistory(trackingResponses);
        }

        return res;
    }

    public DeliveryPartnerResponse mapToPartnerResponse(DeliveryPartner partner) {
        DeliveryPartnerResponse res = new DeliveryPartnerResponse();
        res.setId(partner.getId());
        res.setPartnerName(partner.getPartnerName());
        res.setPartnerCode(partner.getPartnerCode());
        res.setContactNumber(partner.getContactNumber());
        res.setTrackingUrlTemplate(partner.getTrackingUrlTemplate());
        res.setVehicleType(partner.getVehicleType());
        res.setActive(partner.getActive());
        return res;
    }
}
