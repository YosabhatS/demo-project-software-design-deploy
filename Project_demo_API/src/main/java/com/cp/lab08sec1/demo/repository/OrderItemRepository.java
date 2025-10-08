package com.cp.lab08sec1.demo.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cp.lab08sec1.demo.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

}
