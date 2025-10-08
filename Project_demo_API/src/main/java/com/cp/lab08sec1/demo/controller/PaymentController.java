package com.cp.lab08sec1.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cp.lab08sec1.demo.model.MenuItem;
import com.cp.lab08sec1.demo.model.OrderInfo;
import com.cp.lab08sec1.demo.model.OrderItem;
import com.cp.lab08sec1.demo.model.OrderStatus;
import com.cp.lab08sec1.demo.model.Payment;
import com.cp.lab08sec1.demo.repository.MenuItemRepository;
import com.cp.lab08sec1.demo.repository.OrderInfoRepository;
import com.cp.lab08sec1.demo.repository.OrderItemRepository;
import com.cp.lab08sec1.demo.repository.PaymentRepository;
import com.cp.lab08sec1.demo.service.ResourceNotFoundException;

@RestController
@RequestMapping("/api/orders")
public class PaymentController {
	private final OrderInfoRepository orderInfoRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;

    public PaymentController(OrderInfoRepository orderInfoRepository,
            OrderItemRepository orderItemRepository,
            MenuItemRepository menuItemRepository,
            PaymentRepository paymentRepository) {
    	this.orderInfoRepository = orderInfoRepository;
    	this.orderItemRepository = orderItemRepository;
    	this.menuItemRepository = menuItemRepository;
    	this.paymentRepository = paymentRepository;
    }

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<Payment> makePayment(@PathVariable Long orderId) {
        OrderInfo order = orderInfoRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        BigDecimal total = orderItems.stream()
            .map(item -> {
                // ✅ ดึงราคาเมนูจาก menu repository
            	BigDecimal price = menuItemRepository.findById(item.getMenuId())
            		    .map(menu -> BigDecimal.valueOf(menu.getPrice()))   // ✅ แปลง Double → BigDecimal
            		    .orElse(BigDecimal.ZERO);

                return price.multiply(BigDecimal.valueOf(item.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPaidAt(LocalDateTime.now());
        payment.setTotalAmount(total);

        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderInfoRepository.save(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
    }
    
    @PostMapping("/table/{tableId}/payment")
    public ResponseEntity<?> payForTable(@PathVariable Long tableId) {
        // 1. ดึงออเดอร์ทั้งหมดของโต๊ะนี้
        List<OrderInfo> allOrders = orderInfoRepository.findByTableId(tableId);

        if (allOrders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for table " + tableId);
        }

        // 2. ตรวจสอบว่ามี Order ที่ยังอยู่สถานะ CREATED หรือไม่
        boolean hasUnfinished = allOrders.stream()
                .anyMatch(order -> order.getStatus() == OrderStatus.CREATED);

        if (hasUnfinished) {
            // ❌ ถ้ามีออเดอร์ที่ยังไม่เสร็จ → ห้ามจ่าย
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("มีคำสั่งซื้อที่ยังไม่เสร็จ ไม่สามารถชำระเงินได้");
        }

        // 3. รวมยอดจากออเดอร์ที่ COMPLETED เท่านั้น
        List<OrderInfo> ordersToPay = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .toList();

        if (ordersToPay.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("ไม่มีคำสั่งซื้อที่พร้อมชำระเงิน");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderInfo order : ordersToPay) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            BigDecimal orderTotal = items.stream()
                    .map(i -> menuItemRepository.findById(i.getMenuId())
                            .map(m -> BigDecimal.valueOf(m.getPrice()))
                            .orElse(BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalAmount = totalAmount.add(orderTotal);

            // ✅ เปลี่ยนสถานะเป็น PAID
            order.setStatus(OrderStatus.PAID);
            orderInfoRepository.save(order);
        }

        // 4. สร้าง Payment Record
        Payment payment = new Payment();
        payment.setTableId(tableId);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTotalAmount(totalAmount);
        Payment savedPayment = paymentRepository.save(payment);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
    }


    
    

}
