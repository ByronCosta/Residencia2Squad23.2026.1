package com.example.demo.repository;

import com.example.demo.model.EntReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<EntReserva, Long> {

    // Busca por ID da reserva
    List<EntReserva> findAllByIdreserva(Long idreserva);

    // Por sala
    List<EntReserva> findByIdsala(Long idsala);

    // Por usuário
    List<EntReserva> findByIdusuario(Long idusuario);

    // Por profissional
    List<EntReserva> findByIdprofissional(Long idprofissional);

    // Por data inicial exata
    List<EntReserva> findByDatainicial(LocalDate datainicial);

    // Por data final exata
    List<EntReserva> findByDatafinal(LocalDate datafinal);

    // Maior que data inicial (After) e Menor que data final (Before)
    List<EntReserva> findByDatainicialAfterAndDatafinalBefore(LocalDate inicio, LocalDate fim);

    /**
     * CORREÇÃO: O nome do método deve terminar com 'AndHorafinal' para corresponder
     * ao atributo 'horafinal' na sua classe EntReserva.
     */
    List<EntReserva> findByDatainicialAndDatafinalAndHorainicialAndHorafinal(
            LocalDate datainicial,
            LocalDate datafinal,
            java.sql.Time horainicial,
            java.sql.Time horafinal);

    @Query("SELECT count(er) FROM EntReserva r " +
            "JOIN EntEstacaoXReserva er ON r.idreserva = er.idreserva " +
            "WHERE r.idsala = :idsala " +
            "AND NOT (r.datafinal < :dataInicioSolicitada OR r.datainicial > :dataFimSolicitada) " +
            "AND NOT (r.horafinal <= :horaInicial OR r.horainicial >= :horaFinal)")
    Integer somarOcupacaoNoPeriodo(
            @Param("idsala") Long idsala,
            @Param("dataInicioSolicitada") LocalDate dataInicio,
            @Param("dataFimSolicitada") LocalDate dataFim,
            @Param("horaInicial") Time horaInicial,
            @Param("horaFinal") Time horaFinal
    );
}