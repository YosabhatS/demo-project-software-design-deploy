package com.cp.lab08sec1.demo.controller;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cp.lab08sec1.demo.dto.MenuItemDTO;
import com.cp.lab08sec1.demo.dto.OrderInfoDTO;
import com.cp.lab08sec1.demo.dto.OrderItemDTO;
import com.cp.lab08sec1.demo.dto.OrderRequest;
import com.cp.lab08sec1.demo.model.OrderInfo;
import com.cp.lab08sec1.demo.model.OrderItem;
import com.cp.lab08sec1.demo.service.OrderDataService;
import com.cp.lab08sec1.demo.service.ResourceNotFoundException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/data") // Base URL สำหรับ Data Service
public class OrderDataController {

    private final OrderDataService orderDataService;

    public OrderDataController(OrderDataService orderDataService) {
        this.orderDataService = orderDataService;
    }

    // ----------------------------------------------------------------------
    // 🚩 Helper Method: แปลง Blocking Call (Callable) เป็น Mono
    // ----------------------------------------------------------------------
    private <T> Mono<T> toMono(Callable<T> callable) {
        // ใช้ Mono.fromCallable เพื่อย้าย Blocking Call ไปทำงานบน Thread Pool อื่น
        return Mono.fromCallable(callable)
            .onErrorMap(ResourceNotFoundException.class, e -> 
                // แปลง ResourceNotFoundException เป็น Response 404
                new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }


    // ----------------------------------------------------------------------
    // 🚩 1. GET /api/data/orders/{orderId} : ดึง Order Info หลัก
    // ----------------------------------------------------------------------
    @GetMapping("/orders/{orderId}")
    public Mono<ResponseEntity<OrderInfoDTO>> fetchOrderInfo(@PathVariable Long orderId) {
        // 1. เรียก Service (Blocking)
        return toMono(() -> orderDataService.findOrderInfo(orderId))
            // 2. จัดการ Response
            .map(dto -> dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build());
    }


    // ----------------------------------------------------------------------
    // 🚩 2. GET /api/data/orders/{orderId}/items : ดึงรายการ Order Items ย่อย
    // ----------------------------------------------------------------------
    @GetMapping("/orders/{orderId}/items")
    public Flux<OrderItemDTO> fetchOrderItems(@PathVariable Long orderId) {
        // 1. เรียก Service (Blocking) ที่คืนค่าเป็น List
        // 2. แปลง Blocking List ให้เป็น Flux (Stream)
        return Flux.fromIterable(orderDataService.findOrderItems(orderId));
    }


    // ----------------------------------------------------------------------
    // 🚩 3. GET /api/data/menu/{menuId} : ดึงรายละเอียด MenuItem
    // ----------------------------------------------------------------------
    @GetMapping("/menu/{menuId}")
    public Mono<ResponseEntity<MenuItemDTO>> fetchMenuItem(@PathVariable Long menuId) {
        // 1. เรียก Service (Blocking)
        return toMono(() -> orderDataService.findMenuItem(menuId))
            // 2. จัดการ Response
            .map(dto -> dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build());
    }


    // ----------------------------------------------------------------------
    // 🚩 4. POST /api/data/orders : สร้างคำสั่งซื้อใหม่
    // ----------------------------------------------------------------------
    @PostMapping("/orders")
    public Mono<ResponseEntity<OrderInfoDTO>> createNewOrder(@RequestBody OrderRequest request) {
        // 1. เรียก Service (Blocking/Transactional)
        return toMono(() -> orderDataService.createNewOrder(request))
            // 2. จัดการ Response 201 Created
            .map(newOrderDto -> new ResponseEntity<>(newOrderDto, HttpStatus.CREATED));
    }
    
    @GetMapping("/orders/table/{tableId}/current")
    public Mono<ResponseEntity<OrderInfoDTO>> fetchCurrentOrder(@PathVariable Long tableId) {
        return toMono(() -> orderDataService.findCurrentOrder(tableId))
            .map(optionalDto -> optionalDto.map(ResponseEntity::ok)
                                           .orElse(ResponseEntity.notFound().build())); // 200 OK หรือ 404 NOT_FOUND
    }
    
 // ----------------------------------------------------------------------
    // 🚩 NEW: 5. GET /api/data/menu/all : ดึงรายการเมนูทั้งหมด
    // ----------------------------------------------------------------------
    @GetMapping("/menu/all") // <--- Endpoint ที่ Project Web (8082) คาดหวัง
    public Flux<MenuItemDTO> fetchAllMenuItems() {
        // 1. เรียก Service (Blocking) ที่คืนค่าเป็น List
        // 2. แปลง Blocking List ให้เป็น Flux (Stream)
        return toMono(() -> orderDataService.findAllMenuItems()) // แปลง List เป็น Mono
            .flatMapMany(Flux::fromIterable); // แปลง Mono<List> เป็น Flux<MenuItemDTO>
    }
    
    @GetMapping("/orders/status/{status}")
    public Flux<OrderInfoDTO> fetchOrdersByStatus(@PathVariable String status) {
        // แปลง Blocking List ให้เป็น Flux (Stream)
        return Flux.fromIterable(orderDataService.findOrdersByStatus(status));
    }
    
    @PutMapping("/orders/{orderId}/status/{status}")
    public Mono<ResponseEntity<OrderInfoDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @PathVariable String status) {
        
        return toMono(() -> orderDataService.updateOrderStatus(orderId, status))
            .map(dto -> ResponseEntity.ok(dto))
            // แปลง ResourceNotFoundException (จาก Service) เป็น HTTP 404
            .onErrorMap(ResourceNotFoundException.class, e -> 
                new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()))
            // จัดการ Error อื่นๆ เช่น IllegalArgumentException เป็น HTTP 400
            .onErrorMap(IllegalArgumentException.class, e -> 
                new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
    
    @GetMapping("/orders/table/{tableId}/active")
    public Mono<ResponseEntity<OrderInfoDTO>> getActiveOrderByTable(@PathVariable Long tableId) {
        return toMono(() -> orderDataService.findCurrentOrder(tableId))
                .map(optionalDto -> optionalDto
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build()));
    }
    
    @GetMapping("/orders/table/{tableId}/all")
    public Flux<OrderInfoDTO> getAllOrdersByTable(@PathVariable Long tableId) {
        return Flux.fromIterable(orderDataService.findAllOrdersForTable(tableId));
    }
    

}
