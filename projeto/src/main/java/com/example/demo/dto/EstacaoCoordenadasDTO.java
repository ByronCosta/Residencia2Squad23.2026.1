package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor  // CRUCIAL: Cria o construtor vazio que o Jackson precisa para serializar o JSON
public class EstacaoCoordenadasDTO {
    private Long idestacao;
    private int coordx;
    private int coordy;
    private String descricao; // dev, designer, simples

    // Construtor, Getters e Setters (ou @Data / @Builder do Lombok)
    public EstacaoCoordenadasDTO(Long idestacao, int coordx, int coordy, String descricao) {
        this.idestacao = idestacao;
        this.coordx = coordx;
        this.coordy = coordy;
        this.descricao = descricao;
    }
}
