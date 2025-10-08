package com.cp.lab08sec1.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cp.lab08sec1.demo.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
}