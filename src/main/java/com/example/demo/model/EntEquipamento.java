package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "equipamento")
public class EntEquipamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idequipamento")
    private Long idequipamento;
    @NotNull(message = "O ID da estacao é obrigatório") // 👈 Valida na entrada da API (Gera Erro 400)
    @Column(name = "idestacao", nullable = false)
    private Long idestacao;
    @Column(name = "descricao")
    private String descricao;
    @Column(name = "estoque", columnDefinition = "BOOLEAN")
    private Boolean estoque;
}
