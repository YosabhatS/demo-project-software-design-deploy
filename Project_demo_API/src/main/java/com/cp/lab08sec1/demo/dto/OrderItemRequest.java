package com.cp.lab08sec1.demo.dto;

public class OrderItemRequest {
	private Long menuId;
    private Integer quantity;

    // Getters, Setters, Constructors
    
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

}
