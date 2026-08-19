package com.hinchmart.repository;

import com.hinchmart.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByOrderId(Long orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Page<Invoice> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    Page<Invoice> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    List<Invoice> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    boolean existsByOrderId(Long orderId);
}
