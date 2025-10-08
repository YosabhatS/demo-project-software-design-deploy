package com.cp.lab09sec1.service;

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
