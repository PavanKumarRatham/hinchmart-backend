package com.example.hinchmart.repository;

import com.example.hinchmart.entity.ProductBulkPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBulkPriceRepository extends JpaRepository<ProductBulkPrice, Long> {
    List<ProductBulkPrice> findByProductIdOrderByMinQuantityAsc(Long productId);
}
