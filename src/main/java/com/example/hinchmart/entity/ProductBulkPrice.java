package com.example.hinchmart.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_bulk_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBulkPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer minQuantity;

    private Integer maxQuantity;

    private Double price;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getMinQuantity() {
		return minQuantity;
	}

	public void setMinQuantity(Integer minQuantity) {
		this.minQuantity = minQuantity;
	}

	public Integer getMaxQuantity() {
		return maxQuantity;
	}

	public void setMaxQuantity(Integer maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
    
    
}
