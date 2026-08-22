package com.example.hinchmart.repository;

import com.example.hinchmart.entity.RfqQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RfqQuoteRepository extends JpaRepository<RfqQuote, Long> {
    List<RfqQuote> findByRfqId(Long rfqId);
    List<RfqQuote> findBySellerId(Long sellerId);
    Optional<RfqQuote> findByRfqIdAndSellerId(Long rfqId, Long sellerId);
}
