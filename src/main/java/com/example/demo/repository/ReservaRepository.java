package com.example.demo.repository;

import com.example.demo.model.EntReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
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
     * CORREÇÃO: Tipos alterados para LocalTime para evitar o erro de compilação no Service.
     */
    List<EntReserva> findByDatainicialAndDatafinalAndHorainicialAndHorafinal(
            LocalDate datainicial,
            LocalDate datafinal,
            LocalTime horainicial,
            LocalTime horafinal);

    /**
     * CORREÇÃO: Alterado de Time para LocalTime nos parâmetros e no retorno para Long.
     * Esta query JPQL usa os nomes das classes Entidade.
     */
    @Query("SELECT count(er) FROM EntReserva r " +
            "JOIN EntEstacaoXReserva er ON r.idreserva = er.idreserva " +
            "WHERE r.idsala = :idsala " +
            "AND NOT (r.datafinal < :dataInicioSolicitada OR r.datainicial > :dataFimSolicitada) " +
            "AND NOT (r.horafinal <= :horaInicial OR r.horainicial >= :horaFinal)")
    Long somarOcupacaoNoPeriodo(
            @Param("idsala") Long idsala,
            @Param("dataInicioSolicitada") LocalDate dataInicio,
            @Param("dataFimSolicitada") LocalDate dataFim,
            @Param("horaInicial") LocalTime horaInicial,
            @Param("horaFinal") LocalTime horaFinal
    );

    /**
     * QUERY NATIVA: Esta usa os nomes reais das tabelas no banco de dados.
     * Corrigido para LocalTime para alinhar com o DTO e Service.
     */
    @Query(value = "SELECT COUNT(eex.id) FROM reserva er " +
            "JOIN estacaoxreserva eex ON er.idreserva = eex.idreserva " +
            "WHERE er.idsala = :idsala " +
            "AND (er.datainicial <= :dataFinal AND er.datafinal >= :dataInicial) " +
            "AND (er.horainicial < :horaFinal AND er.horafinal > :horaInicial)",
            nativeQuery = true)
    long countOcupacaoConflitante(
            @Param("idsala") Long idsala,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("horaFinal") LocalTime horaFinal,
            @Param("horaInicial") LocalTime horaInicial
    );
}