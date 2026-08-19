package com.hinchmart.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SellerDashboardDto {

    private long totalProducts;
    private long activeProducts;
    private long pendingProducts;
    private long totalOrders;
    private long newOrders;
    private long openRfqs;
    private BigDecimal revenue;
    private long lowStockProducts;
    private List<OrderDto> recentOrders = new ArrayList<>();

    public SellerDashboardDto() {
    }

    public SellerDashboardDto(long totalProducts, long activeProducts, long pendingProducts,
                              long totalOrders, long newOrders, long openRfqs,
                              BigDecimal revenue, long lowStockProducts, List<OrderDto> recentOrders) {
        this.totalProducts = totalProducts;
        this.activeProducts = activeProducts;
        this.pendingProducts = pendingProducts;
        this.totalOrders = totalOrders;
        this.newOrders = newOrders;
        this.openRfqs = openRfqs;
        this.revenue = revenue;
        this.lowStockProducts = lowStockProducts;
        this.recentOrders = recentOrders != null ? recentOrders : new ArrayList<>();
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(long activeProducts) {
        this.activeProducts = activeProducts;
    }

    public long getPendingProducts() {
        return pendingProducts;
    }

    public void setPendingProducts(long pendingProducts) {
        this.pendingProducts = pendingProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getNewOrders() {
        return newOrders;
    }

    public void setNewOrders(long newOrders) {
        this.newOrders = newOrders;
    }

    public long getOpenRfqs() {
        return openRfqs;
    }

    public void setOpenRfqs(long openRfqs) {
        this.openRfqs = openRfqs;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public List<OrderDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderDto> recentOrders) {
        this.recentOrders = recentOrders;
    }
}
