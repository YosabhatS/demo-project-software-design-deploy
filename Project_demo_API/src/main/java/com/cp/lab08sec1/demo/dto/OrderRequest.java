package com.cp.lab08sec1.demo.dto;

import java.util.List;

public class OrderRequest {
	private Long tableId;
    private List<OrderItemRequest> items;

    // Getters, Setters, Constructors
    
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

}
