package com.hinchmart.repository;

import com.hinchmart.entity.Shipment;
import com.hinchmart.entity.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    Optional<Shipment> findByShipmentNumber(String shipmentNumber);
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Page<Shipment> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    List<Shipment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    Page<Shipment> findByStatusOrderByCreatedAtDesc(ShipmentStatus status, Pageable pageable);
    Page<Shipment> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
