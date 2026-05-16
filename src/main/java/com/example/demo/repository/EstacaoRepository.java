package com.example.demo.repository;

import com.example.demo.model.EntEstacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate; // Ajuste o tipo se estiver usando String ou LocalDateTime
import java.util.List;

@Repository
public interface EstacaoRepository extends JpaRepository<EntEstacao, Long> {

    @Query(value = "SELECT * FROM estacao WHERE idsala = :idsala", nativeQuery = true)
    List<EntEstacao> buscarDisponiveisPorSala(@Param("idsala") Long idsala);

    /**
     * Busca todas as estações livres de uma determinada descrição (perfil) no período informado,
     * independente da sala.
     */
    @Query(value = "SELECT e.* FROM estacao e " +
            "WHERE LOWER(e.descricao) = LOWER(:perfil) " +
            "AND e.idestacao NOT IN (" +
            "    SELECT exr.idestacao FROM estacao_x_reserva exr " +
            "    JOIN reserva r ON exr.idreserva = r.idreserva " +
            "    WHERE r.datainicial <= :dataFim AND r.datafinal >= :dataInicio" +
            ")", nativeQuery = true)
    List<EntEstacao> buscarEstacoesLivresPorPerfilSemSala(
            @Param("perfil") String perfil,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}