package com.example.hinchmart.repository;

import com.example.hinchmart.entity.ProductBulkPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBulkPriceRepository extends JpaRepository<ProductBulkPrice, Long> {
    List<ProductBulkPrice> findByProductIdOrderByMinQuantityAsc(Long productId);
}
