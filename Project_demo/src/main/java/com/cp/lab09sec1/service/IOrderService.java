package com.cp.lab09sec1.service;


import com.cp.lab09sec1.dto.OrderInfoDTO;
import com.cp.lab09sec1.dto.OrderRequest;

import reactor.core.publisher.Mono;

public interface IOrderService {
    Mono<OrderInfoDTO> createNewOrder(OrderRequest request); 
    Mono<OrderInfoDTO> getOrderById(Long orderId);

}
