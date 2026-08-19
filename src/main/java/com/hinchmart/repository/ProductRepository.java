package com.hinchmart.repository;

import com.hinchmart.entity.Product;
import com.hinchmart.entity.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);

    Page<Product> findByApprovalStatusAndIsActiveTrue(ApprovalStatus approvalStatus, Pageable pageable);
    long countByApprovalStatusAndIsActiveTrue(ApprovalStatus approvalStatus);
    long countByApprovalStatus(ApprovalStatus approvalStatus);
    List<Product> findBySellerId(Long sellerId);
    Page<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    long countBySellerId(Long sellerId);
    long countBySellerIdAndApprovalStatusAndIsActiveTrue(Long sellerId, ApprovalStatus approvalStatus);
    long countBySellerIdAndApprovalStatus(Long sellerId, ApprovalStatus approvalStatus);
    long countBySellerIdAndStockLessThanEqual(Long sellerId, Integer stock);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByBrandId(Long brandId);

    @Query("SELECT p FROM Product p WHERE " +
           "(:status IS NULL OR p.approvalStatus = :status) AND " +
           "(:isActive IS NULL OR p.isActive = :isActive) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:subcategoryId IS NULL OR p.subcategory.id = :subcategoryId) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:sellerId IS NULL OR p.seller.id = :sellerId) AND " +
           "(:minPrice IS NULL OR p.sellingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.sellingPrice <= :maxPrice) AND " +
           "(:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR (:inStock = false AND p.stock = 0)) AND " +
           "(:query IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProductsFiltered(@Param("query") String query,
                                         @Param("categoryId") Long categoryId,
                                         @Param("subcategoryId") Long subcategoryId,
                                         @Param("brandId") Long brandId,
                                         @Param("sellerId") Long sellerId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice,
                                         @Param("inStock") Boolean inStock,
                                         @Param("status") ApprovalStatus status,
                                         @Param("isActive") Boolean isActive,
                                         Pageable pageable);
}
