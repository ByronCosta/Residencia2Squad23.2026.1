package com.example.demo.repository;

import com.example.demo.model.EntEstacaoXSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstacaoXSalaRepository extends JpaRepository<EntEstacaoXSala, Long> {

    // Busca todos os vínculos de uma estação específica
    List<EntEstacaoXSala> findByIdestacao(Long idestacao);

    // Busca todos os vínculos de uma sala específica
    List<EntEstacaoXSala> findByIdsala(Long idsala);
}