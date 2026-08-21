package com.hinchmart.controller;

import com.hinchmart.dto.request.CreateOrderRequest;
import com.hinchmart.dto.request.OrderStatusUpdateRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.OrderDto;
import com.hinchmart.entity.User;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management (Member 2)", description = "Endpoints for Order Placement, Order Tracking, Status Transitions, and Fulfillment")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(summary = "Place Order from Cart",
            description = "Creates a new order from items in the cart, decrements product inventory, creates audit trail, and clears the cart.")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@RequestParam Long userId,
                                                             @Valid @RequestBody CreateOrderRequest request) {
        User user = authService.getUserById(userId);
        OrderDto order = orderService.createOrder(user.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Order placed successfully", order), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Buyer Orders", description = "Returns a paginated list of orders placed by the specified buyer.")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getMyOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = authService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderDto> orders = orderService.getMyOrders(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/seller")
    @Operation(summary = "Get Seller Received Orders", description = "Returns orders received by the specified seller for fulfillment.")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getSellerOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = authService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orders = orderService.getSellerOrders(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Order Details by ID", description = "Returns full details, line items, and lifecycle status history of an order.")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@RequestParam Long userId,
                                                              @PathVariable Long id) {
        User user = authService.getUserById(userId);
        OrderDto order = orderService.getOrderById(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Order Status",
            description = "Updates order status (e.g. CONFIRMED, PROCESSING, READY_TO_SHIP, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED).")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @RequestParam Long userId,
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        User user = authService.getUserById(userId);
        OrderDto updated = orderService.updateOrderStatus(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated to " + request.getStatus().name(), updated));
    }
}
