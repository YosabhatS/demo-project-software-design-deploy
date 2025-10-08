package com.cp.lab08sec1.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cp.lab08sec1.demo.dto.MenuItemDTO;
import com.cp.lab08sec1.demo.dto.OrderInfoDTO;
import com.cp.lab08sec1.demo.dto.OrderItemDTO;
import com.cp.lab08sec1.demo.dto.OrderRequest;
import com.cp.lab08sec1.demo.model.MenuItem;
import com.cp.lab08sec1.demo.model.OrderInfo;
import com.cp.lab08sec1.demo.model.OrderItem;
import com.cp.lab08sec1.demo.model.OrderStatus;
import com.cp.lab08sec1.demo.repository.MenuItemRepository;
import com.cp.lab08sec1.demo.repository.OrderInfoRepository;
import com.cp.lab08sec1.demo.repository.OrderItemRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderDataService {

    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    
    // ... Constructor Injection ...
    public OrderDataService(OrderInfoRepository orderInfoRepository, OrderItemRepository orderItemRepository,
			MenuItemRepository menuItemRepository) {
		super();
		this.orderInfoRepository = orderInfoRepository;
		this.orderItemRepository = orderItemRepository;
		this.menuItemRepository = menuItemRepository;
	}


 // ----------------------------------------------------------------------
    // 🚩 1. GET: ดึง Order Info หลัก (/api/data/orders/{orderId})
    // ----------------------------------------------------------------------
    public OrderInfoDTO findOrderInfo(Long orderId) {
    	OrderInfo order = orderInfoRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order ID " + orderId + " not found."));

        // 🚩 NEW: ดึงรายการ Order Item ที่เกี่ยวข้อง
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId); // ใช้เมธอดใน OrderItemRepository
        
        // 🚩 NEW: แปลงเป็น DTO และส่งรายการ Items เข้าไป
        return toOrderInfoDTO(order, items); 
    }

    // ----------------------------------------------------------------------
    // 🚩 2. GET: ดึงรายการ Order Items ย่อย (/api/data/orders/{orderId}/items)
    // ----------------------------------------------------------------------
    public List<OrderItemDTO> findOrderItems(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        
        return items.stream()
            .map(this::toOrderItemDTO)
            .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------------
    // 🚩 3. GET: ดึงรายละเอียด MenuItem (/api/data/menu/{menuId})
    // ----------------------------------------------------------------------
    public MenuItemDTO findMenuItem(Long menuId) {
        return menuItemRepository.findById(menuId)
            .map(this::toMenuItemDTO)
            .orElse(null); 
    }

    // ----------------------------------------------------------------------
    // 🚩 4. POST: สร้างคำสั่งซื้อใหม่ (/api/data/orders)
    // ----------------------------------------------------------------------
    @Transactional // ใช้ JTA Transaction (Blocking)
    public OrderInfoDTO createNewOrder(OrderRequest request) throws ResourceNotFoundException {
        
        // --- 1. ตรวจสอบความถูกต้องและดึงราคา MenuItem ---
        List<OrderItem> itemsToSave = request.getItems().stream()
            .map(itemRequest -> {
                // ตรวจสอบว่า MenuItem มีอยู่จริงหรือไม่
                MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu item ID " + itemRequest.getMenuId() + " not found.")
                    );
                
                // สร้าง OrderItem Entity (สำหรับการบันทึก)
                OrderItem item = new OrderItem();
                item.setMenuId(menuItem.getId());
                item.setQuantity(itemRequest.getQuantity());
                return item;
            })
            .collect(Collectors.toList());
            
        // --- 2. บันทึก OrderInfo หลัก ---
        OrderInfo order = new OrderInfo();
        order.setTableId(request.getTableId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        OrderInfo savedOrder = orderInfoRepository.save(order);
        Long orderId = savedOrder.getId();

        // --- 3. บันทึก OrderItems ย่อย ---
        itemsToSave.forEach(item -> {
            item.setOrderId(orderId);
            orderItemRepository.save(item);
        });

        // --- 4. คืนค่า DTO ---
        return toOrderInfoDTO(savedOrder, itemsToSave);
    }
    
    public Optional<OrderInfoDTO> findCurrentOrder(Long tableId) {
        // กำหนดสถานะที่ถือว่า "ปัจจุบัน" หรือ "ยังไม่เสร็จ"
        return orderInfoRepository.findFirstByTableIdAndStatus(tableId, OrderStatus.CREATED) 
               .map(order -> toOrderInfoDTO(order, orderItemRepository.findByOrderId(order.getId())));
    }
    
    public List<MenuItemDTO> findAllMenuItems() {
        List<MenuItem> entities = menuItemRepository.findAll();
        // แปลง List<MenuItem> เป็น List<MenuItemDTO>
        return entities.stream()
            .map(this::toMenuItemDTO)
            .collect(Collectors.toList());
    }
    
    public List<OrderInfoDTO> findOrdersByStatus(String status) {
    	OrderStatus statusEnum = OrderStatus.valueOf(status.toUpperCase());
        return orderInfoRepository.findByStatus(statusEnum).stream()
            .map(this::toOrderInfoDTO) // toOrderInfoDTO คือเมธอดที่ใช้แปลง OrderInfo Entity เป็น OrderInfoDTO
            .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderInfoDTO updateOrderStatus(Long orderId, String newStatus) {
        OrderInfo order = orderInfoRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order ID " + orderId + " not found."));

        // แปลงสถานะให้เป็นตัวพิมพ์ใหญ่ เพื่อป้องกันความผิดพลาด
        OrderStatus statusEnum = OrderStatus.valueOf(newStatus.toUpperCase());
        order.setStatus(statusEnum);
        OrderInfo updatedOrder = orderInfoRepository.save(order);

        return toOrderInfoDTO(updatedOrder);
    }

    // ----------------------------------------------------------------------
    // --- Private Mappers (แปลง Entity เป็น DTO) ---
    // ----------------------------------------------------------------------
    
    private OrderInfoDTO toOrderInfoDTO(OrderInfo entity) {
        OrderInfoDTO dto = new OrderInfoDTO();
        dto.setId(entity.getId());
        dto.setTableId(entity.getTableId());
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private OrderItemDTO toOrderItemDTO(OrderItem entity) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(entity.getId());          // ต้องมี Getter/Setter ใน OrderItemDTO
        dto.setOrderId(entity.getOrderId()); // ต้องมี Getter/Setter ใน OrderItemDTO
        
        dto.setMenuId(entity.getMenuId());
        dto.setQuantity(entity.getQuantity());

        // 🚩 FIX 2: ดึงและ Map รายละเอียด Menu Item
        MenuItem menuItem = menuItemRepository.findById(entity.getMenuId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item ID " + entity.getMenuId() + " not found."));

        dto.setMenuItemDetails(toMenuItemDTO(menuItem)); // เรียกใช้ Mapper toMenuItemDTO
        
        return dto;
    }
    
    private MenuItemDTO toMenuItemDTO(MenuItem entity) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setImageUrl(entity.getImageUrl());
        return dto;
    }
    
    private OrderInfoDTO toOrderInfoDTO(OrderInfo entity, List<OrderItem> items) {
        OrderInfoDTO dto = new OrderInfoDTO();
        dto.setId(entity.getId());
        dto.setTableId(entity.getTableId());
        dto.setStatus(entity.getStatus().name());
        dto.setCreatedAt(entity.getCreatedAt());

        // 🚩 NEW: แปลง List<OrderItem> เป็น List<OrderItemDTO>
        List<OrderItemDTO> itemDTOs = items.stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList());
                
        dto.setItems(itemDTOs); // 🚩 กำหนดรายการ Items
        return dto;
    }
    
    public List<OrderInfoDTO> findAllOrdersForTable(Long tableId) {
        return orderInfoRepository.findFirstByTableIdAndStatus(tableId, OrderStatus.CREATED)
                .stream()
                .map(order -> toOrderInfoDTO(order, orderItemRepository.findByOrderId(order.getId())))
                .collect(Collectors.toList());
    }
    
}
