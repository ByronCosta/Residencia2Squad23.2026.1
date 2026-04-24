package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime; // Sugestão: use LocalTime em vez de java.sql.Time

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reserva")
public class EntReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idreserva")
    private Long idreserva;

    @Column(name = "idsala")
    private Long idsala;

    @Column(name = "idusuario")
    private Long idusuario;

    @Column(name = "idprofissional")
    private Long idprofissional;

    @Column(name = "datainicial")
    private LocalDate datainicial;

    @Column(name = "datafinal")
    private LocalDate datafinal;

    @Column(name = "horainicial")
    private LocalTime horainicial; // LocalTime é a recomendação moderna para o Java 8+

    @Column(name = "horafinal")
    private LocalTime horafinal; // LocalTime é a recomendação moderna para o Java 8+
}