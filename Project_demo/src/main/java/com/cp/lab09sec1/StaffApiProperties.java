package com.cp.lab09sec1;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "staff.api")
public class StaffApiProperties {

    /**
     * Base URL ของ Project_demo_API (ไม่รวม path ย่อย เช่น /login).
     */
    private String baseUrl = "http://localhost:8085/api/staff";

    /**
     * Timeout สูงสุดที่ยอมรับได้ในการเรียก API ของฝั่งฐานข้อมูล.
     */
    private Duration timeout = Duration.ofSeconds(5);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}