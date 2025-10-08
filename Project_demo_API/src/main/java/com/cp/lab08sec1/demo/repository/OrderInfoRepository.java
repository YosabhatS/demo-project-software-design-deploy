package com.cp.lab08sec1.demo.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cp.lab08sec1.demo.model.OrderInfo;
import com.cp.lab08sec1.demo.model.OrderStatus;


public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long>{
	Optional<OrderInfo> findFirstByTableIdAndStatus(Long tableId, OrderStatus status);
	List<OrderInfo> findByStatus(OrderStatus status);
	List<OrderInfo> findByTableIdAndStatus(Long tableId, OrderStatus status);
	List<OrderInfo> findByTableId(Long tableId);


}
