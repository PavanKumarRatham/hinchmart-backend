package com.example.hinchmart.service;

import com.example.hinchmart.dto.*;
import com.example.hinchmart.entity.*;
import com.example.hinchmart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository orderStatusHistoryRepository,
                        CartService cartService,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.cartService = cartService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.notificationService = notificationService;
    }

    /**
     * POST /api/checkout/preview
     * Calculates checkout preview breakdown including subtotal, GST, delivery charge, and grand total.
     */
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse getCheckoutPreview(CheckoutPreviewRequest request) {
        Long buyerId = request != null && request.getBuyerId() != null ? request.getBuyerId() : 1L;
        Double deliveryCharge = (request != null && request.getDeliveryCharge() != null) ? request.getDeliveryCharge() : 2500.0;

        CartResponse cart = cartService.getCart(buyerId);

        double subtotal = cart.getSubtotalAmount() != null ? cart.getSubtotalAmount() : 0.0;
        double gst = cart.getTotalGstAmount() != null ? cart.getTotalGstAmount() : 0.0;
        double total = round(subtotal + gst + deliveryCharge);

        CheckoutPreviewResponse preview = new CheckoutPreviewResponse();
        preview.setSubtotal(round(subtotal));
        preview.setGst(round(gst));
        preview.setDeliveryCharge(round(deliveryCharge));
        preview.setTotal(total);
        preview.setItemCount(cart.getTotalItems() != null ? cart.getTotalItems() : 0);
        preview.setCurrency("INR");
        preview.setItems(cart.getItems());

        return preview;
    }

    /**
     * POST /api/orders
     * Converts buyer's active cart into an Order, records order items, tracks initial status history, and empties cart.
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Long buyerId = request.getBuyerId() != null ? request.getBuyerId() : 1L;
        Double deliveryCharge = request.getDeliveryCharge() != null ? request.getDeliveryCharge() : 2500.0;
        String paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : "NET_BANKING";

        // Retrieve current cart
        Cart cart = cartRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new RuntimeException("Cart not found for buyer id: " + buyerId));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot place order: Cart is empty.");
        }

        // Calculate totals
        double subtotalSum = 0.0;
        double gstSum = 0.0;

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setBuyerId(buyerId);
        order.setAddressId(request.getAddressId());
        order.setShippingAddress(request.getShippingAddress() != null ? request.getShippingAddress() : "Default Delivery Site");
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");
        order.setOrderStatus("PLACED");
        order.setDeliveryCharge(round(deliveryCharge));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Re-validate and compute pricing
            CartService.PriceCalculation calc = cartService.calculateBulkPriceAndGst(product, cartItem.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setSellerId(cartItem.getSellerId());
            orderItem.setSeller(product.getSeller());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(round(calc.unitPrice));
            orderItem.setGstPercentage(calc.gstPercentage);
            orderItem.setGstAmount(round(calc.gstAmount));
            orderItem.setSubtotal(round(calc.subtotal));
            orderItem.setTotalPrice(round(calc.totalWithGst));
            orderItem.setCreatedAt(LocalDateTime.now());

            orderItems.add(orderItem);

            subtotalSum += calc.subtotal;
            gstSum += calc.gstAmount;
        }

        order.setSubtotal(round(subtotalSum));
        order.setGstAmount(round(gstSum));
        order.setTotalAmount(round(subtotalSum + gstSum + deliveryCharge));
        order.setItems(orderItems);

        // Record initial status history
        OrderStatusHistory initialHistory = new OrderStatusHistory();
        initialHistory.setOrder(order);
        initialHistory.setStatus("PLACED");
        initialHistory.setChangedBy("BUYER");
        initialHistory.setRemarks(request.getRemarks() != null ? request.getRemarks() : "Order placed successfully");
        initialHistory.setCreatedAt(LocalDateTime.now());

        order.getStatusHistory().add(initialHistory);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order creation
        cartItemRepository.deleteAll(cartItems);

        // Trigger automatic notifications
        try {
            // 1. Notify Buyer
            notificationService.sendNotification(
                    savedOrder.getBuyerId(),
                    "BUYER",
                    "ORDER_PLACED",
                    "Order Placed Successfully: " + savedOrder.getOrderNumber(),
                    "Your order of ₹" + savedOrder.getTotalAmount() + " has been placed. Payment Method: " + savedOrder.getPaymentMethod(),
                    "ORDER",
                    savedOrder.getId()
            );

            // 2. Notify Sellers for each item in the order
            savedOrder.getItems().stream()
                    .map(OrderItem::getSellerId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .forEach(sellerId -> {
                        notificationService.sendNotification(
                                sellerId,
                                "SELLER",
                                "ORDER_PLACED",
                                "New Order Received: " + savedOrder.getOrderNumber(),
                                "A new order #" + savedOrder.getOrderNumber() + " with total value ₹" + savedOrder.getTotalAmount() + " has been placed.",
                                "ORDER",
                                savedOrder.getId()
                        );
                    });
        } catch (Exception e) {
            // Logging failure without rolling back order transaction
        }

        return mapToOrderResponse(savedOrder);
    }

    /**
     * Get Order details by ID
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    /**
     * Get Order details by Order Number
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    /**
     * Get all Orders for a specific buyer
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByBuyerId(Long buyerId) {
        List<Order> orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    /**
     * Get all Orders (for Admin / Dispatch)
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    /**
     * Update order status with audit history
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String newStatus = request.getStatus().trim().toUpperCase();
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(newStatus);
        history.setChangedBy(request.getChangedBy() != null ? request.getChangedBy() : "SYSTEM");
        history.setRemarks(request.getRemarks() != null ? request.getRemarks() : "Status updated to " + newStatus);
        history.setCreatedAt(LocalDateTime.now());

        order.getStatusHistory().add(history);

        Order updatedOrder = orderRepository.save(order);

        // Automatic notification on Order Status change
        try {
            String notifType = "ORDER_UPDATES";
            String title = "Order #" + updatedOrder.getOrderNumber() + " Status: " + newStatus;
            String message = "Your order status has been updated to " + newStatus + ". Remarks: " + history.getRemarks();

            if ("CONFIRMED".equalsIgnoreCase(newStatus)) {
                notifType = "ORDER_CONFIRMED";
                title = "Order Confirmed: #" + updatedOrder.getOrderNumber();
                message = "Your order has been verified and confirmed by the seller.";
            } else if ("SHIPPED".equalsIgnoreCase(newStatus)) {
                notifType = "ORDER_SHIPPED";
                title = "Order Shipped: #" + updatedOrder.getOrderNumber();
                message = "Your order has been packed and handed over to logistics.";
            } else if ("OUT_FOR_DELIVERY".equalsIgnoreCase(newStatus)) {
                notifType = "OUT_FOR_DELIVERY";
                title = "Out For Delivery: Order #" + updatedOrder.getOrderNumber();
                message = "Your order is out for delivery today and will arrive shortly.";
            } else if ("DELIVERED".equalsIgnoreCase(newStatus)) {
                notifType = "ORDER_DELIVERED";
                title = "Order Delivered: #" + updatedOrder.getOrderNumber();
                message = "Your order has been successfully delivered. Thank you for choosing HinchMart!";
            }

            notificationService.sendNotification(
                    updatedOrder.getBuyerId(),
                    "BUYER",
                    notifType,
                    title,
                    message,
                    "ORDER",
                    updatedOrder.getId()
            );
        } catch (Exception e) {
            // Notification dispatch fault tolerance
        }

        return mapToOrderResponse(updatedOrder);
    }

    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomSuffix = 100 + new Random().nextInt(900);
        return "ORD-" + dateStr + "-" + randomSuffix;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setBuyerId(order.getBuyerId());
        response.setAddressId(order.getAddressId());
        response.setShippingAddress(order.getShippingAddress());
        response.setSubtotal(order.getSubtotal());
        response.setGstAmount(order.getGstAmount());
        response.setDeliveryCharge(order.getDeliveryCharge());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderStatus(order.getOrderStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse ir = new OrderItemResponse();
            ir.setId(item.getId());
            ir.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
            ir.setProductName(item.getProduct() != null ? item.getProduct().getProductName() : null);
            ir.setSku(item.getProduct() != null ? item.getProduct().getSku() : null);
            ir.setSeller(item.getSeller());
            ir.setSellerId(item.getSellerId());
            ir.setUnit(item.getProduct() != null ? item.getProduct().getUnit() : null);
            ir.setQuantity(item.getQuantity());
            ir.setUnitPrice(item.getUnitPrice());
            ir.setSubtotal(item.getSubtotal());
            ir.setGstPercentage(item.getGstPercentage());
            ir.setGstAmount(item.getGstAmount());
            ir.setTotalPrice(item.getTotalPrice());
            return ir;
        }).collect(Collectors.toList());

        List<OrderStatusHistoryResponse> historyResponses = order.getStatusHistory().stream().map(h -> {
            OrderStatusHistoryResponse hr = new OrderStatusHistoryResponse();
            hr.setId(h.getId());
            hr.setStatus(h.getStatus());
            hr.setChangedBy(h.getChangedBy());
            hr.setRemarks(h.getRemarks());
            hr.setCreatedAt(h.getCreatedAt());
            return hr;
        }).collect(Collectors.toList());

        response.setItems(itemResponses);
        response.setStatusHistory(historyResponses);

        return response;
    }
}
