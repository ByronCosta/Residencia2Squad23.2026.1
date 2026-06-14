package com.example.demo.dto;

import com.example.demo.model.EntEstacao;
import com.example.demo.model.EntSala;

import java.util.List;

public class SalaComEstacoesDTO {
    private EntSala sala;
    private List<EntEstacao> estacoes;

    public SalaComEstacoesDTO(EntSala sala, List<EntEstacao> estacoes) {
        this.sala = sala;
        this.estacoes = estacoes;
    }

    // Getters e Setters
    public EntSala getSala() { return sala; }
    public void setSala(EntSala sala) { this.sala = sala; }
    public List<EntEstacao> getEstacoes() { return estacoes; }
    public void setEstacoes(List<EntEstacao> estacoes) { this.estacoes = estacoes; }
}