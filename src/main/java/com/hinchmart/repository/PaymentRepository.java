package com.hinchmart.repository;

import com.hinchmart.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
    List<Payment> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
}
