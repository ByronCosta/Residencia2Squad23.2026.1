package com.example.demo.dto;

import com.example.demo.model.UserRole;

public record UserDTO(Long id, String email, UserRole role) {}