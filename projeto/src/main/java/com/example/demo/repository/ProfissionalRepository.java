package com.example.demo.repository;

import com.example.demo.model.EntProfissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<EntProfissional, Long> {

    // Busca um profissional pelo email (que vem da classe pai User)
    Optional<EntProfissional> findByEmail(String email);

    // Busca profissionais que contenham determinada palavra no perfil
    // Ex: "Java", "Redes", "Médico"
    List<EntProfissional> findByPerfilContainingIgnoreCase(String perfil);

    // Verifica se já existe um profissional com esse email
    boolean existsByEmail(String email);
}