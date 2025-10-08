package com.cp.lab09sec1.dto;

public class OrderItemDTO {
	private Long id;            // order_item id
    private Long orderId;       // order_id (Foreign Key)
    private Long menuId;        // menu_id (Foreign Key)
    private Integer quantity;   // จำนวน
    private MenuItemDTO menuItemDetails;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public MenuItemDTO getMenuItemDetails() { return menuItemDetails; }
    public void setMenuItemDetails(MenuItemDTO menuItemDetails) { this.menuItemDetails = menuItemDetails; }
    

}
