package com.example.hinchmart.repository;

import com.example.hinchmart.entity.ShipmentTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long> {
    List<ShipmentTracking> findByShipmentIdOrderByEventTimeAsc(Long shipmentId);
}
