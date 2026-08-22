package com.example.hinchmart.repository;

import com.example.hinchmart.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByShipmentNumber(String shipmentNumber);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    List<Shipment> findByOrderId(Long orderId);
    List<Shipment> findBySellerId(Long sellerId);
    List<Shipment> findByStatus(String status);
    List<Shipment> findBySellerIdAndStatus(Long sellerId, String status);
}
