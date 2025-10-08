package com.cp.lab09sec1.entity;

public class UserAccount {
    private final String username;
    private final String password;
    private final UserRole role;
    private final String displayName;

    public UserAccount(String username, String password, UserRole role, String displayName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }
}
