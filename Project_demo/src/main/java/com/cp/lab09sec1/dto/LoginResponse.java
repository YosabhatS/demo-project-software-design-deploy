package com.cp.lab09sec1.dto;

public class LoginResponse {
    private final boolean success;
    private final String role;
    private final String displayName;
    private final String message;

    public LoginResponse(boolean success, String role, String displayName, String message) {
        this.success = success;
        this.role = role;
        this.displayName = displayName;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMessage() {
        return message;
    }
}
