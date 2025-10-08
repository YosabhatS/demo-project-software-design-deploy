package com.cp.lab09sec1.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Corresponds to 'id'

    @Column(name = "name", nullable = false)
    private String name; // Corresponds to 'name' (ชื่ออาหาร)

    @Column(name = "price", nullable = false)
    private Double price; // Corresponds to 'price'
    
    @Column(name = "imageUrl", nullable = false)
    private String imageUrl; // Corresponds to 'price';
    
    public MenuItem() {
    	
    }

	public MenuItem(Long id, String name, Double price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
