package com.hinchmart.repository;

import com.hinchmart.entity.Rfq;
import com.hinchmart.entity.enums.RfqStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqRepository extends JpaRepository<Rfq, Long> {
    Optional<Rfq> findByRfqNumber(String rfqNumber);
    List<Rfq> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    Page<Rfq> findByStatusOrderByCreatedAtDesc(RfqStatus status, Pageable pageable);
    long countByStatus(RfqStatus status);
    Page<Rfq> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
