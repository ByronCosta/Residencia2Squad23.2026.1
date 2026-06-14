package com.example.demo.model;


public enum UserRole {
    ADMIN("admin"),
    PROFISSIONAL("profissional"),
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