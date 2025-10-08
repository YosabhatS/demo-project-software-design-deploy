package com.cp.lab09sec1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cp.lab09sec1.dto.MenuItemDTO;
import com.cp.lab09sec1.dto.OrderInfoDTO;
import com.cp.lab09sec1.dto.OrderRequest;
import com.cp.lab09sec1.dto.PaymentReceiptDTO;
import com.cp.lab09sec1.service.OrderService;
import com.cp.lab09sec1.service.RemoteServiceException;
import com.cp.lab09sec1.service.ResourceNotFoundException;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController // 🚩 ใช้ @RestController ถ้า Controller นี้คืนค่า JSON โดยตรง (ซึ่งคาดว่าเป็นเช่นนั้น)
@RequestMapping("/api/orders") // 🚩 NEW: Base URL สำหรับ Web/Client Service
public class OrderController {
	private static final String DATA_SERVICE_URL = "http://localhost:8085"; 
	private final OrderService orderService; 
	private final org.springframework.web.client.RestTemplate restTemplate;

    // Constructor Injection
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
        this.restTemplate = new org.springframework.web.client.RestTemplate();
    }
    @PostMapping("/table/{tableId}/payment")
    public ResponseEntity<PaymentReceiptDTO> payForTable(@PathVariable Long tableId) {
        String targetUrl = DATA_SERVICE_URL + "/api/orders/table/" + tableId + "/payment";
        ResponseEntity<PaymentReceiptDTO> response = restTemplate.postForEntity(targetUrl, null,
                PaymentReceiptDTO.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
    
    @GetMapping("/menu")
    public Flux<MenuItemDTO> getAllMenuItems() {
        return orderService.getAllMenuItems()
            // ไม่ต้องใช้ ResponseEntity เพราะ Flux จะจัดการการ Streaming ให้อยู่แล้ว
            
            // จัดการ Error: Remote Service Failure (ถ้า Data Service ล่ม)
            .onErrorResume(RemoteServiceException.class, e -> {
                // คืนค่าเป็น 503 Service Unavailable (โดยการโยน Exception ที่ WebFlux จัดการ)
                return Flux.error(new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Data Service is unavailable to fetch menu"
                ));
            });
    }
    
    @GetMapping("/kitchen")
    public Flux<OrderInfoDTO> getKitchenOrders() {
        return orderService.getOrdersByStatus("CREATED")
                .onErrorResume(RemoteServiceException.class, e -> 
                    // คืนค่า 503 SERVICE_UNAVAILABLE 
                    Flux.error(new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "Data Service is unavailable to fetch kitchen orders.")));
    }
    
    @PutMapping("/{orderId}/status/completed")
    public Mono<ResponseEntity<OrderInfoDTO>> completeOrder(@PathVariable Long orderId) {
        return orderService.completeOrder(orderId)
                .map(updatedOrder -> new ResponseEntity<>(updatedOrder, HttpStatus.OK))
                .onErrorResume(ResourceNotFoundException.class, e -> 
                    Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)))
                .onErrorResume(RemoteServiceException.class, e -> 
                    Mono.just(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE)));
    }


    // ----------------------------------------------------------------------
    // 🚩 1. GET /api/orders/table/{tableId} : จุดเริ่มต้นเมื่อสแกน QR Code
    // ----------------------------------------------------------------------
    /**
     * ตรวจสอบ Order ปัจจุบันของโต๊ะ: 
     * - ถ้ามี Order ค้างอยู่ (Status CREATED) จะคืนค่า Order นั้น (200 OK)
     * - ถ้าไม่มี Order ค้างอยู่ จะคืนค่า 404 NOT FOUND (Web Client สามารถสร้าง Order ใหม่ได้)
     */
    @GetMapping("/table/{tableId}")
    public Mono<ResponseEntity<OrderInfoDTO>> getActiveOrderForTable(@PathVariable Long tableId) {
        
        return orderService.findActiveOrderByTable(tableId)
            // เมื่อสำเร็จ: คืนค่า 200 OK พร้อม OrderInfoDTO
            .map(order -> new ResponseEntity<>(order, HttpStatus.OK))
            
            // จัดการ Error: Resource Not Found (Order ปัจจุบันไม่พบ)
            .onErrorResume(ResourceNotFoundException.class, e -> 
                // คืนค่า 404 Not Found (ไม่มี Order ปัจจุบันของโต๊ะนี้)
                Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND))) 
            
            // จัดการ Error: Remote Service Failure 
            .onErrorResume(RemoteServiceException.class, e -> 
                // คืนค่า 503 Service Unavailable
                Mono.just(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE)));
    }


    // ----------------------------------------------------------------------
    // 🚩 2. POST /api/orders : สร้างคำสั่งซื้อใหม่ (ใช้ tableId จาก Request Body)
    // ----------------------------------------------------------------------
    /**
     * สร้างคำสั่งซื้อใหม่
     */
    @PostMapping
    public Mono<ResponseEntity<OrderInfoDTO>> createNewOrder(@Valid @RequestBody OrderRequest request) {
        
        // 💡 Request ที่เข้ามาจะต้องมี Table ID ที่ได้จากการสแกน QR Code แล้ว
        return orderService.createNewOrder(request)
            .map(newOrder -> new ResponseEntity<>(newOrder, HttpStatus.CREATED))
            // ... (Error Handling เหมือนเดิม)
            .onErrorResume(ResourceNotFoundException.class, e -> 
                Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)))
            .onErrorResume(RemoteServiceException.class, e -> 
                Mono.just(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE)));
    }
    
    // ----------------------------------------------------------------------
    // 🚩 3. GET /api/orders/{orderId} : ดึงรายละเอียดคำสั่งซื้อ (เมธอดเดิม)
    // ----------------------------------------------------------------------
    @GetMapping("/{orderId}")
    public Mono<ResponseEntity<OrderInfoDTO>> getOrderDetails(@PathVariable Long orderId) {
        // ... (โค้ดเดิม) ...
        return orderService.getOrderById(orderId)
            .map(order -> new ResponseEntity<>(order, HttpStatus.OK))
            .onErrorResume(ResourceNotFoundException.class, e -> 
                Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)))
            .onErrorResume(RemoteServiceException.class, e -> 
                Mono.just(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE)));
    }
    
    @PostMapping("/{orderId}/payment")
    public ResponseEntity<PaymentReceiptDTO> forwardPayment(@PathVariable Long orderId) {
        String url = DATA_SERVICE_URL + "/api/orders/" + orderId + "/payment";
        ResponseEntity<PaymentReceiptDTO> response = restTemplate.postForEntity(url, null,
                PaymentReceiptDTO.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/status/{status}")
    public Flux<OrderInfoDTO> getOrdersByStatus(@PathVariable String status) {
        return orderService.getOrdersByStatus(status)
                .onErrorResume(ResourceNotFoundException.class, e -> Flux.error(
                        new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
                .onErrorResume(RemoteServiceException.class, e -> Flux.error(
                        new org.springframework.web.server.ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "Data Service is unavailable to fetch orders by status.")));
    }
    
    @GetMapping("/tables/{tableId}")
    public ResponseEntity<String> getTableInfo(@PathVariable Long tableId) {
        String url = DATA_SERVICE_URL + "/api/tables/" + tableId;
        return restTemplate.getForEntity(url, String.class);
    }


}
