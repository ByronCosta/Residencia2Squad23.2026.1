package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Método essencial para buscar o utilizador pelo e-mail durante o login
    Optional<User> findByEmail(String email);
}
