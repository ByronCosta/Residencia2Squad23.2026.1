package com.example.demo.repository;

import com.example.demo.model.EntSala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.example.demo.model.EntSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<EntSala, Long> {

    // Antes era findByDisponivel, agora deve ser:
    List<EntSala> findByDisponibilidade(Boolean disponibilidade);

    List<EntSala> findByEnderecoContainingIgnoreCase(String endereco);

    // Antes terminava em AndDisponivel, agora:
    List<EntSala> findByEnderecoContainingIgnoreCaseAndDisponibilidade(String endereco, Boolean disponibilidade);
}