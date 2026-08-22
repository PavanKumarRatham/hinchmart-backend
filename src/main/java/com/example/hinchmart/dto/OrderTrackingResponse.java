package com.example.hinchmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private Long orderId;
    private String orderNumber;
    private String orderStatus;
    private Long buyerId;
    private String shippingAddress;
    private Double totalAmount;
    private LocalDateTime orderDate;
    private ShipmentResponse activeShipment;
    private List<ShipmentResponse> allShipments = new ArrayList<>();
    private List<OrderStatusHistoryResponse> statusHistory = new ArrayList<>();
}
