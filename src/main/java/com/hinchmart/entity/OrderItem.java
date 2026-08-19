package com.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hinchmart.entity.enums.ProductUnit;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order", columnList = "order_id"),
    @Index(name = "idx_order_item_product", columnList = "product_id"),
    @Index(name = "idx_order_item_seller", columnList = "seller_id")
})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "items"})
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User seller;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(length = 100)
    private String sku;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductUnit unit = ProductUnit.PIECE;

    @Column(name = "gst_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal gstPercentage = new BigDecimal("18.00");

    @Column(name = "gst_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal gstAmount;

    @Column(name = "total_price", precision = 14, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    public OrderItem() {
    }

    public OrderItem(Order order, Product product, User seller, String productName, String sku,
                     BigDecimal unitPrice, Integer quantity, ProductUnit unit,
                     BigDecimal gstPercentage, BigDecimal gstAmount, BigDecimal totalPrice) {
        this.order = order;
        this.product = product;
        this.seller = seller;
        this.productName = productName;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.unit = unit != null ? unit : ProductUnit.PIECE;
        this.gstPercentage = gstPercentage != null ? gstPercentage : new BigDecimal("18.00");
        this.gstAmount = gstAmount;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public ProductUnit getUnit() {
        return unit;
    }

    public void setUnit(ProductUnit unit) {
        this.unit = unit;
    }

    public BigDecimal getGstPercentage() {
        return gstPercentage;
    }

    public void setGstPercentage(BigDecimal gstPercentage) {
        this.gstPercentage = gstPercentage;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
