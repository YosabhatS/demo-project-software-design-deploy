package com.cp.lab09sec1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Corresponds to 'id'

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Corresponds to 'quantity'

    // Relationship with OrderInfo (Foreign Key: order_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false) // Corresponds to 'order_id'
    private OrderInfo orderInfo;

    // Relationship with MenuItem (Foreign Key: menu_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false) // Corresponds to 'menu_id'
    private MenuItem menuItem;

	public OrderItem(Long id, Integer quantity, OrderInfo orderInfo, MenuItem menuItem) {
		super();
		this.id = id;
		this.quantity = quantity;
		this.orderInfo = orderInfo;
		this.menuItem = menuItem;
	}
	public OrderItem() {

	}

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

	public OrderInfo getOrderInfo() {
		return orderInfo;
	}

	public void setOrderInfo(OrderInfo orderInfo) {
		this.orderInfo = orderInfo;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}
    
}
