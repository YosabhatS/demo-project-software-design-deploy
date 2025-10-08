package com.cp.lab09sec1.entity;

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
