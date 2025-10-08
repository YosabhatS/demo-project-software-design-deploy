package com.cp.lab09sec1.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Corresponds to 'id'

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount; // Corresponds to 'total_amount'

    @Column(name = "paid_at")
    private LocalDateTime paidAt; // Corresponds to 'paid_at'

    // Relationship with OrderInfo (Foreign Key: order_id)
    // One-to-One relationship where Payment is the owner (JoinColumn)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true, nullable = false) // Corresponds to 'order_id'
    private OrderInfo orderInfo;

	public Payment(Long id, Double totalAmount, LocalDateTime paidAt, OrderInfo orderInfo) {
		super();
		this.id = id;
		this.totalAmount = totalAmount;
		this.paidAt = paidAt;
		this.orderInfo = orderInfo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(LocalDateTime paidAt) {
		this.paidAt = paidAt;
	}

	public OrderInfo getOrderInfo() {
		return orderInfo;
	}

	public void setOrderInfo(OrderInfo orderInfo) {
		this.orderInfo = orderInfo;
	}
}
