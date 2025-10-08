package com.cp.lab08sec1.demo.service;

public class ResourceNotFoundException extends RuntimeException {

    // Constructor ที่รับข้อความ (message)
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructor ที่รับข้อความและสาเหตุ (cause)
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
