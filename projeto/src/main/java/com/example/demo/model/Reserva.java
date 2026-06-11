package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    private Long idreserva;
    private Long idsala;
    private Long idusuario;
    private Long idprofissional;
    private Data datainicial;
    private Data datafinal;
    private Time horainicial;
    private Time horafinal;/*quantidade de minutos*/

}
