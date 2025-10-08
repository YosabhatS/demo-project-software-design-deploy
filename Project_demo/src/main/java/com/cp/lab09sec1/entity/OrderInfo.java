package com.cp.lab09sec1.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "order_info")
public class OrderInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Corresponds to 'id'

    @Column(name = "status", nullable = false)
    private String status; // Corresponds to 'status' (e.g., PENDING, PAID, CANCELLED)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // Corresponds to 'created_at'

    // Relationship with TableInfo (Foreign Key: table_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false) // Corresponds to 'table_id'
    private TableInfo tableInfo;

    // Relationship with OrderItem (One-to-Many)
    @OneToMany(mappedBy = "orderInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    // Relationship with Payment (One-to-One or One-to-Many depending on business logic, here assumed One-to-One)
    @OneToOne(mappedBy = "orderInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

	public OrderInfo(Long id, String status, LocalDateTime createdAt, TableInfo tableInfo, List<OrderItem> items,
			Payment payment) {
		super();
		this.id = id;
		this.status = status;
		this.createdAt = createdAt;
		this.tableInfo = tableInfo;
		this.items = items;
		this.payment = payment;
	}
	public OrderInfo() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}
    
}
