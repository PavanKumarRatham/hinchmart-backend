package com.hinchmart.dto.request;

import com.hinchmart.entity.enums.ProductUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long subcategoryId;

    private Long brandId;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String hsnCode;

    private BigDecimal gstRate = new BigDecimal("18.00");

    @NotNull(message = "MOQ (Minimum Order Quantity) is required")
    private Integer moq = 1;

    @NotNull(message = "Unit is required (Piece, Bag, Kg, Ton, Meter, Feet, Liter, Box, Set, Bundle, Roll, Sq.Ft)")
    private ProductUnit unit = ProductUnit.PIECE;

    @NotNull(message = "MRP is required")
    private BigDecimal mrp;

    @NotNull(message = "Selling price is required")
    private BigDecimal sellingPrice;

    @NotNull(message = "Stock is required")
    private Integer stock = 0;

    private Integer deliveryDays = 3;

    private String description;

    private String specifications;

    private List<String> imageUrls = new ArrayList<>();

    private List<BulkPriceTierDto> bulkPrices = new ArrayList<>();

    public ProductCreateRequest() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getHsnCode() {
        return hsnCode;
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    public BigDecimal getGstRate() {
        return gstRate;
    }

    public void setGstRate(BigDecimal gstRate) {
        this.gstRate = gstRate;
    }

    public Integer getMoq() {
        return moq;
    }

    public void setMoq(Integer moq) {
        this.moq = moq;
    }

    public ProductUnit getUnit() {
        return unit;
    }

    public void setUnit(ProductUnit unit) {
        this.unit = unit;
    }

    public BigDecimal getMrp() {
        return mrp;
    }

    public void setMrp(BigDecimal mrp) {
        this.mrp = mrp;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<BulkPriceTierDto> getBulkPrices() {
        return bulkPrices;
    }

    public void setBulkPrices(List<BulkPriceTierDto> bulkPrices) {
        this.bulkPrices = bulkPrices;
    }
}
