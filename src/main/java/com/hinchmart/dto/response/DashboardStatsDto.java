package com.hinchmart.dto.response;

public class DashboardStatsDto {

    private long totalBuyers;
    private long totalSellers;
    private long pendingSellers;
    private long activeProducts;
    private long openRfqs;
    private long todayOrders;
    private long totalCategories;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalBuyers, long totalSellers, long pendingSellers,
                             long activeProducts, long openRfqs, long todayOrders, long totalCategories) {
        this.totalBuyers = totalBuyers;
        this.totalSellers = totalSellers;
        this.pendingSellers = pendingSellers;
        this.activeProducts = activeProducts;
        this.openRfqs = openRfqs;
        this.todayOrders = todayOrders;
        this.totalCategories = totalCategories;
    }

    public long getTotalBuyers() {
        return totalBuyers;
    }

    public void setTotalBuyers(long totalBuyers) {
        this.totalBuyers = totalBuyers;
    }

    public long getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(long totalSellers) {
        this.totalSellers = totalSellers;
    }

    public long getPendingSellers() {
        return pendingSellers;
    }

    public void setPendingSellers(long pendingSellers) {
        this.pendingSellers = pendingSellers;
    }

    public long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(long activeProducts) {
        this.activeProducts = activeProducts;
    }

    public long getOpenRfqs() {
        return openRfqs;
    }

    public void setOpenRfqs(long openRfqs) {
        this.openRfqs = openRfqs;
    }

    public long getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(long todayOrders) {
        this.todayOrders = todayOrders;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }
}
