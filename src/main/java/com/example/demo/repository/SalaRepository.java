package com.example.demo.repository;

import com.example.demo.model.EntSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<EntSala, Long> {

    List<EntSala> findByDisponibilidade(Boolean disponibilidade);

    List<EntSala> findByEnderecoContainingIgnoreCase(String endereco);

    List<EntSala> findByEnderecoContainingIgnoreCaseAndDisponibilidade(String endereco, Boolean disponibilidade);

    @Query("SELECT s FROM EntSala s " +
            "WHERE s.disponibilidade = true " +
            "AND (SELECT COUNT(e) FROM EntEstacao e WHERE e.idsala = s.idsala AND e.descricao = 'dev') >= :qtdDev " +
            "AND (SELECT COUNT(e) FROM EntEstacao e WHERE e.idsala = s.idsala AND e.descricao = 'design') >= :qtdDesign")
    List<EntSala> buscarSalasPorCapacidadeDePerfis(
            @Param("qtdDev") Long qtdDev,
            @Param("qtdDesign") Long qtdDesign
    );
}