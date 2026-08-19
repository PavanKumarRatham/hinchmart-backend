package com.hinchmart.repository;

import com.hinchmart.entity.RfqQuote;
import com.hinchmart.entity.enums.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqQuoteRepository extends JpaRepository<RfqQuote, Long> {
    List<RfqQuote> findByRfqIdOrderByCreatedAtDesc(Long rfqId);
    Page<RfqQuote> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    List<RfqQuote> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    Optional<RfqQuote> findByRfqIdAndSellerId(Long rfqId, Long sellerId);
    long countBySellerId(Long sellerId);
    long countByRfqId(Long rfqId);
    long countByRfqIdAndStatus(Long rfqId, QuoteStatus status);
}
