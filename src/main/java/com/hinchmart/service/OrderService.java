package com.hinchmart.service;

import com.hinchmart.dto.request.AddToCartRequest;
import com.hinchmart.dto.request.CheckoutPreviewRequest;
import com.hinchmart.dto.request.CreateOrderRequest;
import com.hinchmart.dto.request.OrderStatusUpdateRequest;
import com.hinchmart.dto.response.*;
import com.hinchmart.entity.*;
import com.hinchmart.entity.enums.OrderStatus;
import com.hinchmart.entity.enums.PaymentMethod;
import com.hinchmart.entity.enums.PaymentStatus;
import com.hinchmart.entity.enums.Role;
import com.hinchmart.exception.BadRequestException;
import com.hinchmart.exception.ResourceNotFoundException;
import com.hinchmart.exception.UnauthorizedException;
import com.hinchmart.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository orderStatusHistoryRepository,
                        CartService cartService,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        ActivityLogService activityLogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public CheckoutPreviewDto previewCheckout(Long buyerUserId, CheckoutPreviewRequest request) {
        CartDto cartDto = cartService.getCart(buyerUserId);
        if (cartDto.getItems() == null || cartDto.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Add products to cart before previewing checkout.");
        }

        BigDecimal deliveryCharge = new BigDecimal("2500.00");
        CheckoutPreviewDto preview = new CheckoutPreviewDto(
                cartDto.getSubtotal(),
                cartDto.getGstTotal(),
                deliveryCharge,
                cartDto.getSubtotal().add(cartDto.getGstTotal()).add(deliveryCharge)
        );
        preview.setItems(cartDto.getItems());
        return preview;
    }

    @Transactional
    public OrderDto createOrder(Long buyerUserId, CreateOrderRequest request) {
        User buyer = userRepository.findById(buyerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer user not found: " + buyerUserId));

        Cart cart = cartService.getOrCreateCart(buyerUserId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty. Cannot place an order.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstAmount = BigDecimal.ZERO;
        BigDecimal deliveryCharge = new BigDecimal("2500.00");

        Order order = new Order();
        order.setOrderNumber("ORD-" + System.currentTimeMillis() % 10000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setBuyer(buyer);
        order.setShippingAddress(request.getShippingAddress());
        order.setBillingAddress(request.getBillingAddress() != null ? request.getBillingAddress() : request.getShippingAddress());
        order.setCity(request.getCity());
        order.setState(request.getState());
        order.setPincode(request.getPincode());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.UPI);
        order.setPaymentStatus(request.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY ? PaymentStatus.PENDING : PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setNotes(request.getNotes());

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            // Re-validate product & stock
            cartService.validateProductForCart(product, qty);

            BigDecimal unitPrice = cartService.calculateBulkUnitPrice(product, qty);
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gstRate = product.getGstRate() != null ? product.getGstRate() : new BigDecimal("18.00");
            BigDecimal itemGst = itemSubtotal.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal itemTotal = itemSubtotal.add(itemGst);

            // Deduct stock
            product.setStock(product.getStock() - qty);
            if (product.getInventory() != null) {
                product.getInventory().setQuantity(product.getStock());
            }
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getSeller(),
                    product.getProductName(),
                    product.getSku(),
                    unitPrice,
                    qty,
                    product.getUnit(),
                    gstRate,
                    itemGst,
                    itemTotal
            );
            orderItems.add(orderItem);
            order.addItem(orderItem);

            subtotal = subtotal.add(itemSubtotal);
            gstAmount = gstAmount.add(itemGst);
        }

        order.setSubtotal(subtotal);
        order.setGstAmount(gstAmount);
        order.setDeliveryCharge(deliveryCharge);
        order.setTotalAmount(subtotal.add(gstAmount).add(deliveryCharge));

        // Initial status history
        OrderStatusHistory history = new OrderStatusHistory(order, OrderStatus.PLACED, "Order created and confirmed by buyer", buyer);
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(buyerUserId);

        activityLogService.log(buyerUserId, buyer.getEmail(), "ORDER_PLACED", "ORDER",
                savedOrder.getId(), "Placed order " + savedOrder.getOrderNumber() + " with total amount ₹" + savedOrder.getTotalAmount(), null);

        return mapToOrderDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId, Long currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (currentUser.getRole() == Role.BUYER && !order.getBuyer().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to view this order");
        }

        return mapToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getMyOrders(Long buyerUserId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerUserId, pageable);
        List<OrderDto> dtos = orders.getContent().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, orders.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getSellerOrders(Long sellerUserId, Pageable pageable) {
        Page<Order> orders = orderRepository.findOrdersBySellerId(sellerUserId, pageable);
        List<OrderDto> dtos = orders.getContent().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, orders.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<OrderDto> dtos = orders.getContent().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, orders.getTotalElements());
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, Long currentUserId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        order.setOrderStatus(request.getStatus());

        OrderStatusHistory history = new OrderStatusHistory(
                order,
                request.getStatus(),
                request.getNotes() != null ? request.getNotes() : "Status updated to " + request.getStatus().name(),
                currentUser
        );
        order.addStatusHistory(history);
        orderStatusHistoryRepository.save(history);

        Order saved = orderRepository.save(order);

        activityLogService.log(currentUserId, currentUser.getEmail(), "ORDER_STATUS_CHANGED", "ORDER",
                orderId, "Order status changed to " + request.getStatus().name(), null);

        return mapToOrderDto(saved);
    }

    public OrderDto mapToOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setCity(order.getCity());
        dto.setState(order.getState());
        dto.setPincode(order.getPincode());
        dto.setSubtotal(order.getSubtotal());
        dto.setGstAmount(order.getGstAmount());
        dto.setDeliveryCharge(order.getDeliveryCharge());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getBuyer() != null) {
            dto.setBuyerId(order.getBuyer().getId());
            dto.setBuyerName(order.getBuyer().getFullName());
            dto.setBuyerEmail(order.getBuyer().getEmail());
            dto.setBuyerPhone(order.getBuyer().getPhone());
            if (order.getBuyer().getBuyerProfile() != null) {
                dto.setBuyerCompanyName(order.getBuyer().getBuyerProfile().getCompanyName());
            }
        }

        if (order.getItems() != null) {
            List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
                OrderItemDto iDto = new OrderItemDto();
                iDto.setId(item.getId());
                if (item.getProduct() != null) {
                    iDto.setProductId(item.getProduct().getId());
                }
                iDto.setProductName(item.getProductName());
                iDto.setSku(item.getSku());
                iDto.setUnitPrice(item.getUnitPrice());
                iDto.setQuantity(item.getQuantity());
                iDto.setUnit(item.getUnit());
                iDto.setGstPercentage(item.getGstPercentage());
                iDto.setGstAmount(item.getGstAmount());
                iDto.setTotalPrice(item.getTotalPrice());

                if (item.getSeller() != null) {
                    iDto.setSellerId(item.getSeller().getId());
                    iDto.setSellerName(item.getSeller().getFullName());
                    if (item.getSeller().getSellerProfile() != null) {
                        iDto.setSellerCompanyName(item.getSeller().getSellerProfile().getCompanyName());
                    }
                }
                return iDto;
            }).collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        if (order.getStatusHistory() != null) {
            List<OrderStatusHistoryDto> historyDtos = order.getStatusHistory().stream().map(h -> {
                OrderStatusHistoryDto hDto = new OrderStatusHistoryDto();
                hDto.setId(h.getId());
                hDto.setStatus(h.getStatus());
                hDto.setNotes(h.getNotes());
                hDto.setCreatedAt(h.getCreatedAt());
                if (h.getChangedBy() != null) {
                    hDto.setChangedByUserId(h.getChangedBy().getId());
                    hDto.setChangedByUserName(h.getChangedBy().getFullName());
                }
                return hDto;
            }).collect(Collectors.toList());
            dto.setStatusHistory(historyDtos);
        }

        return dto;
    }
}
