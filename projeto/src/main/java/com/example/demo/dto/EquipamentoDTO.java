package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipamentoDTO {
    private Long idequipamento;
    private Long idestacao;
    private String descricao;
    private Boolean estoque;
}
