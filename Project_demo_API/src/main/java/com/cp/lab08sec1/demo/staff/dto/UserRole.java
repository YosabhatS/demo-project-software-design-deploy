package com.cp.lab08sec1.demo.staff.dto;

public enum UserRole {
    CHEF,
    EMPLOYEE;

    public String getDisplayName() {
        return switch (this) {
            case CHEF -> "Chef";
            case EMPLOYEE -> "Employee";
        };
    }
}
