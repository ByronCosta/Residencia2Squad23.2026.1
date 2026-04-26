package com.example.demo.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estacao {

    private Long idestacao;
    private String descricao;
    private Integer coordx;
    private Integer coordy;

    // Campo transiente (não vai para o banco) usado apenas para o cálculo do algoritmo
    private Double distanciaReferencia;
}