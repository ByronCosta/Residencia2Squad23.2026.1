package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {

    private Long idreserva;
    private Long idsala;
    private Long idusuario;
    private Long idprofissional;
    private LocalDate datainicial;
    private LocalDate datafinal;
    private Time horainicial;
    private Time horafinal;

}
