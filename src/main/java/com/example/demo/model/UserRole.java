package com.example.demo.model;


public enum UserRole {
    ADMIN("admin"),
    LIDER("lider"),
    USER("user");

    private String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}