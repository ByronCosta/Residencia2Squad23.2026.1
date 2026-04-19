package com.example.demo.repository;

import com.example.demo.model.EntReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<EntReserva, Long> {

    // CORREÇÃO: O nome deve bater com o atributo 'idreserva' da EntReserva
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

    // CORREÇÃO: Tipos ajustados para bater com a EntReserva (Time e Double)
    List<EntReserva> findByDatainicialAndDatafinalAndHorainicialAndTempo(
            LocalDate datainicial,
            LocalDate datafinal,
            java.sql.Time horainicial,
            Double tempo);
}