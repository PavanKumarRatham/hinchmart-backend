package com.hinchmart.repository;

import com.hinchmart.entity.Order;
import com.hinchmart.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items item WHERE item.seller.id = :sellerId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items item WHERE item.seller.id = :sellerId ORDER BY o.createdAt DESC")
    List<Order> findTop5OrdersBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items item WHERE item.seller.id = :sellerId")
    long countOrdersBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items item WHERE item.seller.id = :sellerId AND o.orderStatus = :status")
    long countOrdersBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(item.totalPrice), 0) FROM OrderItem item WHERE item.seller.id = :sellerId AND item.order.orderStatus NOT IN ('CANCELLED', 'RETURNED')")
    BigDecimal calculateSellerRevenue(@Param("sellerId") Long sellerId);

    long countByOrderStatus(OrderStatus orderStatus);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
