package com.example.hinchmart.controller;

import com.example.hinchmart.dto.*;
import com.example.hinchmart.service.ShipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /**
     * POST /api/seller/orders/{id}/shipment
     * Seller creates a shipment for an order after packing.
     */
    @PostMapping("/api/seller/orders/{id}/shipment")
    public ResponseEntity<?> createShipment(
            @PathVariable("id") Long orderId,
            @RequestBody CreateShipmentRequest request) {
        try {
            ShipmentResponse response = shipmentService.createShipment(orderId, request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/orders/{id}/tracking
     * Buyer, Seller or Admin gets real-time tracking timeline for an order.
     */
    @GetMapping("/api/orders/{id}/tracking")
    public ResponseEntity<?> getOrderTracking(@PathVariable("id") Long orderId) {
        try {
            OrderTrackingResponse response = shipmentService.getOrderTracking(orderId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/seller/shipments/{id}/status
     * Update shipment status (e.g. PICKUP_SCHEDULED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, etc.)
     */
    @PatchMapping("/api/seller/shipments/{id}/status")
    public ResponseEntity<?> updateShipmentStatus(
            @PathVariable("id") Long shipmentId,
            @RequestBody ShipmentStatusUpdateRequest request) {
        try {
            if (request.getStatus() == null || request.getStatus().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status field is required."));
            }
            ShipmentResponse response = shipmentService.updateShipmentStatus(shipmentId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/admin/shipments
     * Admin view of all marketplace shipments with optional filters.
     */
    @GetMapping("/api/admin/shipments")
    public ResponseEntity<List<ShipmentResponse>> getAdminShipments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long sellerId) {
        List<ShipmentResponse> shipments = shipmentService.getAdminShipments(status, sellerId);
        return ResponseEntity.ok(shipments);
    }

    /**
     * GET /api/delivery-partners
     * List all active integrated logistics delivery partners (BlueDart, Delhivery, VRL, Rivigo, GATI).
     */
    @GetMapping("/api/delivery-partners")
    public ResponseEntity<List<DeliveryPartnerResponse>> getDeliveryPartners() {
        List<DeliveryPartnerResponse> partners = shipmentService.getDeliveryPartners();
        return ResponseEntity.ok(partners);
    }
}
