package com.example.hinchmart.repository;

import com.example.hinchmart.entity.SellerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerDocumentRepository extends JpaRepository<SellerDocument, Long> {
    List<SellerDocument> findBySellerId(Long sellerId);
    List<SellerDocument> findBySeller(String seller);
    List<SellerDocument> findByStatus(String status);
    List<SellerDocument> findByDocumentType(String documentType);
}
