package com.cp.lab09sec1.service;



import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.cp.lab09sec1.dto.MenuItemDTO;
import com.cp.lab09sec1.dto.OrderInfoDTO;
import com.cp.lab09sec1.dto.OrderItemDTO;
import com.cp.lab09sec1.dto.OrderRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DataServiceClient {
	private final WebClient dataWebClient;

    public DataServiceClient(WebClient dataWebClient) {
        this.dataWebClient = dataWebClient;
    }
    
    private ResponseSpec handleErrors(RequestHeadersUriSpec<?> spec, String uri, Object... uriVariables) {
        return spec.uri(uri, uriVariables) // ใช้ uriVariables เพื่อรองรับ Path variables
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                      clientResponse -> {
                          String message = String.format("Data Service failed for %s. Status: %s", 
                                                        uri, clientResponse.statusCode());
                          // คืนค่า Exception ที่เหมาะสม
                          return Mono.error(new RemoteServiceException(message));
                      });
    }

    public Mono<OrderInfoDTO> fetchOrderInfo(Long orderId) {
        return dataWebClient.get()
            .uri("/api/data/orders/{orderId}", orderId)
            .retrieve()
            // ... Error Handling
            .bodyToMono(OrderInfoDTO.class);
    }
    
    public Flux<OrderItemDTO> fetchOrderItems(Long orderId) {
        return dataWebClient.get()
            .uri("/api/data/orders/{orderId}/items", orderId)
            .retrieve()
            // ... Error Handling
            .bodyToFlux(OrderItemDTO.class);
    }
    
    public Mono<MenuItemDTO> fetchMenuItem(Long menuId) {
        return dataWebClient.get()
            .uri("/api/data/menu/{menuId}", menuId)
            .retrieve()
            // ... Error Handling
            .bodyToMono(MenuItemDTO.class);
    }
    public Mono<OrderInfoDTO> createOrder(OrderRequest request) {
        // ใช้ OrderRequest เป็น body ในการส่ง
    	return dataWebClient.post()
                .uri("/api/data/orders") // Set URI first
                .bodyValue(request)      // Set the request body
                .retrieve()              // Start the response processing
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(), 
                    clientResponse -> {
                        String message = String.format("Data Service failed for /api/data/orders. Status: %s", 
                                                        clientResponse.statusCode());
                        return Mono.error(new RemoteServiceException(message));
                    }
                )
                .bodyToMono(OrderInfoDTO.class);
    }
    public Mono<OrderInfoDTO> fetchActiveOrderForTable(Long tableId) {
        return dataWebClient.get()
            // 1. กำหนด URI ไปยัง Endpoint ใหม่ของ Data Service
            .uri("/api/data/orders/table/{tableId}", tableId) 
            .retrieve()
            
            // 2. จัดการ 404 (ถ้า API บอกว่า "ไม่พบ Order Active")
            .onStatus(status -> status.value() == 404, 
                      clientResponse -> 
                        // โยน Exception ที่ Web Controller เข้าใจว่าคือ 404 
                        Mono.error(new ResourceNotFoundException("No active order found for table " + tableId)))
                        
            // 3. จัดการ Error อื่นๆ (4xx/5xx)
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                      clientResponse -> {
                          String message = String.format("Data Service failed for active order check. Status: %s", 
                                                        clientResponse.statusCode());
                          return Mono.error(new RemoteServiceException(message));
                      })
            .bodyToMono(OrderInfoDTO.class);
    }
    
    public Flux<MenuItemDTO> fetchAllMenuItems() {
        return dataWebClient.get()
            .uri("/api/data/menu/all") // <--- สมมติว่า Data Service (8085) มี Endpoint นี้
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                      clientResponse -> {
                          String message = String.format("Data Service failed for /api/data/menu/all. Status: %s", 
                                                        clientResponse.statusCode());
                          return Mono.error(new RemoteServiceException(message));
                      })
            .bodyToFlux(MenuItemDTO.class); // คาดหวังการคืนค่าเป็น Stream ของ DTO
    }
    
 // NEW: ดึง Order ทั้งหมดตามสถานะ (เช่น 'CREATED')
    public Flux<OrderInfoDTO> fetchOrdersByStatus(String status) {
        return dataWebClient.get()
            .uri("/api/data/orders/status/{status}", status)
            .retrieve()
            .onStatus(statusCode  -> statusCode .is4xxClientError() || statusCode .is5xxServerError(), 
                      clientResponse -> {
                          String message = String.format("Data Service failed for /api/data/orders/status. Status: %s", 
                                                        clientResponse.statusCode());
                          return Mono.error(new RemoteServiceException(message));
                      })
            .bodyToFlux(OrderInfoDTO.class);
    }

    // NEW: อัปเดตสถานะ Order
    public Mono<OrderInfoDTO> updateOrderStatus(Long orderId, String newStatus) {
        // ใช้ PUT เพื่ออัปเดตสถานะ โดยส่งสถานะใหม่เป็น Body หรือ Path Variable
        return dataWebClient.put()
            .uri("/api/data/orders/{orderId}/status/{status}", orderId, newStatus)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                      clientResponse -> {
                          String message = String.format("Data Service failed for PUT status. Status: %s", 
                                                        clientResponse.statusCode());
                          return Mono.error(new RemoteServiceException(message));
                      })
            .bodyToMono(OrderInfoDTO.class);
    }

}
