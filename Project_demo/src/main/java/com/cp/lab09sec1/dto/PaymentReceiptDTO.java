package com.cp.lab09sec1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO สำหรับรับข้อมูลใบเสร็จจากบริการ Data Service
 * และส่งกลับไปยัง Front-end เพื่อแสดงผลในหน้าคิดเงินของพนักงาน
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentReceiptDTO {
    private Long orderId;
    private Long tableId;
    private Double totalAmount;
    private String paidAt;
    private String message;

    public PaymentReceiptDTO() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}