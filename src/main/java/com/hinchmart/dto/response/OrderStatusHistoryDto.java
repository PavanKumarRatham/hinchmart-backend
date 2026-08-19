package com.hinchmart.dto.response;

import com.hinchmart.entity.enums.OrderStatus;
import java.time.LocalDateTime;

public class OrderStatusHistoryDto {

    private Long id;
    private OrderStatus status;
    private String notes;
    private Long changedByUserId;
    private String changedByUserName;
    private LocalDateTime createdAt;

    public OrderStatusHistoryDto() {
    }

    public OrderStatusHistoryDto(Long id, OrderStatus status, String notes,
                                 Long changedByUserId, String changedByUserName, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.notes = notes;
        this.changedByUserId = changedByUserId;
        this.changedByUserName = changedByUserName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedByUserName() {
        return changedByUserName;
    }

    public void setChangedByUserName(String changedByUserName) {
        this.changedByUserName = changedByUserName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
