package com.example.hinchmart.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rfq_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RfqItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private String unit;

    private String remarks;

    @ManyToOne
    @JoinColumn(name = "rfq_id", nullable = false)
    @JsonBackReference
    private Rfq rfq;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Rfq getRfq() {
		return rfq;
	}

	public void setRfq(Rfq rfq) {
		this.rfq = rfq;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
    
    
}