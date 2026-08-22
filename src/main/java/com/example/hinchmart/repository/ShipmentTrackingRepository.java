package com.example.hinchmart.repository;

import com.example.hinchmart.entity.ShipmentTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long> {
    List<ShipmentTracking> findByShipmentIdOrderByEventTimeAsc(Long shipmentId);
}
