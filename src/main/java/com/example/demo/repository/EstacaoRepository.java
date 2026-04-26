package com.example.demo.repository;

import com.example.demo.model.EntEstacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstacaoRepository extends JpaRepository<EntEstacao, Long> {

    @Query(value = "SELECT e.* FROM estacao e " +
            "JOIN estacaoxsala exs ON e.idestacao = exs.idestacao " +
            "WHERE exs.idsala = :idsala",
            nativeQuery = true)
    List<EntEstacao> buscarDisponiveisPorSala(@Param("idsala") Long idsala);
}