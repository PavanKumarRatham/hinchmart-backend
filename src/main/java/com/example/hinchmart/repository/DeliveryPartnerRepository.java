package com.example.hinchmart.repository;

import com.example.hinchmart.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByPartnerCode(String partnerCode);
    List<DeliveryPartner> findByActiveTrue();
}
