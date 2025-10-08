package com.cp.lab09sec1.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cp.lab09sec1.dto.MenuItemDTO;
import com.cp.lab09sec1.dto.OrderInfoDTO;
import com.cp.lab09sec1.dto.OrderItemDTO;
import com.cp.lab09sec1.dto.OrderItemRequest;
import com.cp.lab09sec1.dto.OrderRequest;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService implements IOrderService{
	@Autowired
	 private final DataServiceClient dataServiceClient;
	 public OrderService(DataServiceClient dataServiceClient) {
	        this.dataServiceClient = dataServiceClient;
	 }
	 
	 @Override
	    public Mono<OrderInfoDTO> getOrderById(Long orderId) {
	        
	        // 1. ดึง OrderInfo หลัก (Mono<OrderInfoDTO>)
	        Mono<OrderInfoDTO> orderMono = dataServiceClient.fetchOrderInfo(orderId)
	            .switchIfEmpty(Mono.error(() -> new ResourceNotFoundException("Order ID " + orderId + " not found remotely.")));

	        // 2. ดึง OrderItems ที่เกี่ยวข้อง (Flux<OrderItemDTO>)
	        Flux<OrderItemDTO> itemsFlux = dataServiceClient.fetchOrderItems(orderId);

	        // 3. ดึงรายละเอียด MenuItem สำหรับแต่ละ OrderItem (Enrichment)
	        Flux<OrderItemDTO> enrichedItemsFlux = itemsFlux
	            .flatMap(orderItem -> 
	                // เรียก API ดึงรายละเอียดรายการอาหารแบบ Non-Blocking
	                dataServiceClient.fetchMenuItem(orderItem.getMenuId()) 
	                    .map(menuItemDTO -> {
	                        // ผูก DTO รายละเอียดกลับเข้ากับ OrderItemDTO
	                        orderItem.setMenuItemDetails(menuItemDTO); 
	                        return orderItem;
	                    })
	                    // หากเรียก API ล้มเหลว ให้ใช้ OrderItem เดิมโดยไม่มีรายละเอียด (Graceful Degradation)
	                    .defaultIfEmpty(orderItem) 
	            );

	        // 4. รวม OrderInfo Mono เข้ากับ List ของ OrderItems ที่ Enriched แล้ว (collectList)
	        return orderMono.zipWith(enrichedItemsFlux.collectList(), (order, items) -> {
	            order.setItems(items);
	            return order;
	        });
	    }

	    // -----------------------------------------------------------------------
	    // 🚩 2. POST: สร้าง Order ใหม่
	    // -----------------------------------------------------------------------
	    @Override
	    public Mono<OrderInfoDTO> createNewOrder(OrderRequest request) {
	        
	        // 1. ตรวจสอบความถูกต้องของรายการอาหารทั้งหมด (Consistency Check)
	        
	        // แปลงรายการ OrderItemRequest เป็น Flux
	        Flux<OrderItemRequest> itemRequestsFlux = Flux.fromIterable(request.getItems());

	        // ตรวจสอบและดึงข้อมูลราคา/ชื่อของแต่ละรายการ (Enrichment/Validation)
	        Flux<OrderItemDTO> validatedItemsFlux = itemRequestsFlux
	            .flatMap(req -> 
	                dataServiceClient.fetchMenuItem(req.getMenuId()) // เรียก API ดึงรายละเอียด
	                    .map(menuItemDTO -> {
	                        // ถ้าเจอ, สร้าง OrderItemDTO เพื่อส่งไปยัง Data Service
	                        OrderItemDTO itemDto = new OrderItemDTO();
	                        // เราส่งแค่ ID และ Quantity เพราะรายละเอียดอื่นๆ (ราคา, ชื่อ)
	                        // ควรถูกคำนวณ/บันทึกโดย Data Service เพื่อความถูกต้อง
	                        itemDto.setMenuId(menuItemDTO.getId()); 
	                        itemDto.setQuantity(req.getQuantity());
	                        return itemDto;
	                    })
	                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Menu item " + req.getMenuId() + " not found.")))
	            );

	        // 2. ส่ง OrderRequest พร้อมรายการย่อยที่ตรวจสอบแล้วไปยัง Data Service
	        return validatedItemsFlux.collectList()
	            .flatMap(validatedItemsList -> {
	                
	                // 💡 สร้าง Final Request Object (ใช้ OrderRequest เป็นฐาน)
	                // เราจะใช้ OrderRequest เป็นทั้ง Input และเป็นโครงสร้างในการส่งข้อมูล
	                
	                OrderRequest finalRequest = new OrderRequest();
	                finalRequest.setTableId(request.getTableId());
	                
	                // แปลง validatedItemsList (List<OrderItemDTO>) กลับไปเป็น 
	                // List<OrderItemRequest> เพื่อให้เข้ากับโครงสร้างของ OrderRequest
	                List<OrderItemRequest> requestItemsForDataService = validatedItemsList.stream()
	                    .map(dto -> {
	                        OrderItemRequest req = new OrderItemRequest();
	                        req.setMenuId(dto.getMenuId());
	                        req.setQuantity(dto.getQuantity());
	                        return req;
	                    })
	                    .collect(Collectors.toList());
	                    
	                finalRequest.setItems(requestItemsForDataService); 
	                
	                // 3. เรียก API ภายนอกเพื่อสร้าง Order
	                return dataServiceClient.createOrder(finalRequest);
	            });
	            // 💡 ACID Principle: Atomicity จะถูกรับประกันโดย Data Service ผ่าน Transaction
	    }
	    
	    public Mono<OrderInfoDTO> findActiveOrderByTable(Long tableId) {
	        
	        // 1. เรียกใช้เมธอดใหม่ใน DataServiceClient
	        return dataServiceClient.fetchActiveOrderForTable(tableId)
	                
	                // 2. ไม่ต้องทำอะไรเพิ่ม แค่ส่งต่อ Mono ไปยัง Controller
	                //    Exception (ResourceNotFound, RemoteServiceException) จะถูกส่งต่อไปให้ OrderController จัดการเอง
	                .onErrorResume(RemoteServiceException.class, Mono::error)
	                .onErrorResume(ResourceNotFoundException.class, Mono::error);
	    }
	    public Flux<MenuItemDTO> getAllMenuItems() {
	        // 1. เรียก DataServiceClient
	        return dataServiceClient.fetchAllMenuItems()
	            // 2. จัดการ Error (ถ้า Data Service ล่มหรือมีปัญหา)
	            .onErrorResume(RemoteServiceException.class, Mono::error);
	            
	        // ไม่ต้องจัดการ ResourceNotFound เพราะถ้าไม่มีเมนูเลย จะคืนค่าเป็น Flux ว่าง
	    }
	    
	    public Flux<OrderInfoDTO> getOrdersByStatus(String status) {
	        // 1. ดึง OrderInfo หลักทั้งหมดตามสถานะ (Flux<OrderInfoDTO>)
	        return dataServiceClient.fetchOrdersByStatus(status)
	            // 2. ใช้ flatMap เพื่อ Enriched ข้อมูลของแต่ละ Order 
	            .flatMap(orderInfo -> {
	                // 2.1 ดึง OrderItems ที่เกี่ยวข้อง (Flux<OrderItemDTO>)
	                Flux<OrderItemDTO> itemsFlux = dataServiceClient.fetchOrderItems(orderInfo.getId());

	                // 2.2 ดึงรายละเอียด MenuItem สำหรับแต่ละ OrderItem
	                Flux<OrderItemDTO> enrichedItemsFlux = itemsFlux
	                    .flatMap(itemDto -> dataServiceClient.fetchMenuItem(itemDto.getMenuId())
	                        .map(menuItemDto -> {
	                            // ✅ แก้ไข: ใช้ setMenuItemDetails() แทน setName()
	                            itemDto.setMenuItemDetails(menuItemDto); 
	                            return itemDto;
	                        })
	                        // หากไม่พบ Menu (fetchMenuItem รีเทิร์น 404/Empty Mono) 
	                        // เรายังคง OrderItem เดิมไว้ (itemDto ที่ไม่มีรายละเอียดเมนู)
	                        .switchIfEmpty(Mono.just(itemDto)) 
	                    );
	                
	                // 3. รวม OrderInfo หลัก กับ enrichedItemsList
	                return enrichedItemsFlux.collectList()
	                    .map(itemsList -> {
	                        orderInfo.setItems(itemsList); // orderInfo.items คือ List<OrderItemDTO>
	                        return orderInfo;
	                    });
	            });
	    }
	    
	    public Mono<OrderInfoDTO> completeOrder(Long orderId) {
	        // 1. เรียก Data Service เพื่ออัปเดตสถานะเป็น "COMPLETED"
	        return dataServiceClient.updateOrderStatus(orderId, "COMPLETED");
	    }

}
