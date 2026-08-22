package com.example.hinchmart.repository;

import com.example.hinchmart.entity.SellerDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerDocumentRepository extends JpaRepository<SellerDocument, Long> {
    List<SellerDocument> findBySellerId(Long sellerId);
    List<SellerDocument> findBySeller(String seller);
    List<SellerDocument> findByStatus(String status);
    List<SellerDocument> findByDocumentType(String documentType);
}
