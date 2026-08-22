package com.example.hinchmart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RfqRequest {

    private Long buyerId;

    private String deliveryLocation;

    private String requiredDate;

    private List<RfqItemRequest> items;

	public Long getBuyerId() {
		return buyerId;
	}

	public void setBuyerId(Long buyerId) {
		this.buyerId = buyerId;
	}

	public String getDeliveryLocation() {
		return deliveryLocation;
	}

	public void setDeliveryLocation(String deliveryLocation) {
		this.deliveryLocation = deliveryLocation;
	}

	public String getRequiredDate() {
		return requiredDate;
	}

	public void setRequiredDate(String requiredDate) {
		this.requiredDate = requiredDate;
	}

	public List<RfqItemRequest> getItems() {
		return items;
	}

	public void setItems(List<RfqItemRequest> items) {
		this.items = items;
	}
    
}
