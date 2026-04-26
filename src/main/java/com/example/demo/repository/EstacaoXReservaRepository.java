package com.example.demo.repository;

import com.example.demo.model.EntEstacaoXReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface EstacaoXReservaRepository extends JpaRepository<EntEstacaoXReserva, Long> {

    // Mantendo os métodos automáticos do Spring Data
    List<EntEstacaoXReserva> findByIdestacao(Long idestacao);
    List<EntEstacaoXReserva> findByIdreserva(Long idreserva);

    /**
     * Conta quantas estações distintas estão ocupadas em um período.
     * Usei SQL Nativo para garantir que a junção entre estacaoxreserva e reserva funcione
     * sem precisar mapear @ManyToOne nas Entities.
     */
    @Query(value = "SELECT COUNT(DISTINCT er.idestacao) FROM estacaoxreserva er " +
            "JOIN reserva r ON er.idreserva = r.idreserva " +
            "WHERE (r.datainicial <= :dataFim AND r.datafinal >= :dataInicio) " +
            "AND (r.horainicial < :horaFim AND r.horafinal > :horaInicio)",
            nativeQuery = true)
    Long contarEstacoesOcupadasNoPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );
}