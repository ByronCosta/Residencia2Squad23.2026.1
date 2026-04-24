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

    // Busca todos os vínculos de uma estação específica
    List<EntEstacaoXReserva> findByIdestacao(Long idestacao);

    // Busca todos os vínculos de uma reserva específica
    List<EntEstacaoXReserva> findByIdreserva(Long idreserva);

    @Query("SELECT COUNT(er) FROM EntEstacaoXReserva er " +
            "JOIN EntReserva r ON er.idreserva = r.idreserva " +
            "WHERE (r.datainicial <= :dataFim AND r.datafinal >= :dataInicio) " +
            "AND (r.horainicial < :horaFim AND r.horafinal > :horaInicio)")
    Long contarEstacoesOcupadasNoPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim
    );
}
