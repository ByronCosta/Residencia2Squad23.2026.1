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
public class Sala {

    private Long idsala;
    private String endereco;
    private Boolean disponibilidade;
    private Long lot_max;

}