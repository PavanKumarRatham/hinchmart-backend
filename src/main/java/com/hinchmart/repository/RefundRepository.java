package com.hinchmart.repository;

import com.hinchmart.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByRefundNumber(String refundNumber);
    List<Refund> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
    List<Refund> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
