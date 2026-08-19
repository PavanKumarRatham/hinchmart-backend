package com.hinchmart.controller;

import com.hinchmart.dto.request.CreateOrderRequest;
import com.hinchmart.dto.request.OrderStatusUpdateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.OrderDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management (Member 2)", description = "Endpoints for Order Placement, Order Tracking, Status Transitions, and Fulfillment")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Place Order from Cart",
            description = "Creates a new order from items in the cart, decrements product inventory, creates audit trail, and clears the cart.")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(Authentication authentication,
                                                             @Valid @RequestBody CreateOrderRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        OrderDto order = orderService.createOrder(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Order placed successfully", order), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get My Orders (Buyer)", description = "Returns a paginated list of orders placed by the current buyer.")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = authService.getCurrentUser(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderDto> orders = orderService.getMyOrders(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/seller")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get Seller Received Orders", description = "Returns orders received by the authenticated seller for fulfillment.")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getSellerOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = authService.getCurrentUser(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orders = orderService.getSellerOrders(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get Order Details by ID", description = "Returns full details, line items, and lifecycle status history of an order.")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(Authentication authentication,
                                                              @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        OrderDto order = orderService.getOrderById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update Order Status",
            description = "Updates order status (e.g. CONFIRMED, PROCESSING, READY_TO_SHIP, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED).")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        OrderDto updated = orderService.updateOrderStatus(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated to " + request.getStatus().name(), updated));
    }
}
