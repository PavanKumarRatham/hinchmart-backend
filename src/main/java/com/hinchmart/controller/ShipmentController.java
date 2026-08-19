package com.hinchmart.controller;

import com.hinchmart.dto.request.CreateShipmentRequest;
import com.hinchmart.dto.request.UpdateShipmentStatusRequest;
import com.hinchmart.dto.response.ApiResponse;
import com.hinchmart.dto.response.ShipmentDto;
import com.hinchmart.entity.User;
import com.hinchmart.entity.enums.ShipmentStatus;
import com.hinchmart.service.AuthService;
import com.hinchmart.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Logistics & Shipments (Member 2)", description = "Endpoints for Seller Shipment Booking, Courier Tracking, Milestone Status Transitions, and Logistics Management")
@SecurityRequirement(name = "Bearer Authentication")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final AuthService authService;

    public ShipmentController(ShipmentService shipmentService, AuthService authService) {
        this.shipmentService = shipmentService;
        this.authService = authService;
    }

    @PostMapping("/seller/orders/{id}/shipment")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create Shipment for Order (Seller)",
            description = "Seller creates shipment, books carrier, generates tracking/AWB numbers, and sets order status to READY_TO_SHIP.")
    public ResponseEntity<ApiResponse<ShipmentDto>> createShipment(Authentication authentication,
                                                                   @PathVariable Long id,
                                                                   @RequestBody(required = false) CreateShipmentRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        CreateShipmentRequest req = (request != null) ? request : new CreateShipmentRequest();
        ShipmentDto shipment = shipmentService.createShipment(id, user.getId(), req);
        return new ResponseEntity<>(ApiResponse.success("Shipment booked successfully", shipment), HttpStatus.CREATED);
    }

    @GetMapping("/orders/{id}/tracking")
    @PreAuthorize("hasAnyRole('BUYER', 'SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get Order Tracking History",
            description = "Retrieves live courier tracking status, carrier info, estimated delivery date, and chronological checkpoint events.")
    public ResponseEntity<ApiResponse<ShipmentDto>> getTracking(Authentication authentication,
                                                                @PathVariable Long id) {
        User user = authService.getCurrentUser(authentication.getName());
        ShipmentDto tracking = shipmentService.getTrackingByOrderId(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(tracking));
    }

    @PatchMapping("/seller/shipments/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update Shipment Milestone Status",
            description = "Updates shipment milestone (e.g. PICKED_UP, IN_TRANSIT, REACHED_DESTINATION, OUT_FOR_DELIVERY, DELIVERED), syncs order status, and logs notifications.")
    public ResponseEntity<ApiResponse<ShipmentDto>> updateStatus(Authentication authentication,
                                                                 @PathVariable Long id,
                                                                 @Valid @RequestBody UpdateShipmentStatusRequest request) {
        User user = authService.getCurrentUser(authentication.getName());
        ShipmentDto updated = shipmentService.updateShipmentStatus(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Shipment status updated to " + request.getStatus().name(), updated));
    }

    @GetMapping("/admin/shipments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List All Shipments (Admin)", description = "Returns all marketplace shipments with status filtering and pagination.")
    public ResponseEntity<ApiResponse<Page<ShipmentDto>>> getAdminShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShipmentDto> shipments = shipmentService.getAllShipments(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(shipments));
    }
}
