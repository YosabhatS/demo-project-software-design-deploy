package com.cp.lab09sec1.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderInfoDTO {
	private Long id;            // order id
    private Long tableId;       // table_id (Foreign Key)
    private String status;      // สถานะคำสั่งซื้อ
    private LocalDateTime createdAt; // เวลาสร้าง
    
    // 💡 Field สำหรับเก็บรายการย่อยของคำสั่งซื้อที่ถูก Enriched แล้ว
    private List<OrderItemDTO> items;

    // Getters, Setters, Constructors

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

}
