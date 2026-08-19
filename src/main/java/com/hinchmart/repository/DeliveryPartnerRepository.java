package com.hinchmart.repository;

import com.hinchmart.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByCode(String code);
    List<DeliveryPartner> findByIsActiveTrueOrderByNameAsc();
    boolean existsByCode(String code);
}
