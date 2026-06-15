package com.example.demo.dto;

import com.example.demo.model.EntSala;
import com.example.demo.model.EntEstacao;
import java.util.List;

public class SalasEEstacoesDisponiveisDTO {
    private EntSala sala;
    private List<EntEstacao> estacoesDisponiveis;

    public SalasEEstacoesDisponiveisDTO(EntSala sala, List<EntEstacao> estacoesDisponiveis) {
        this.sala = sala;
        this.estacoesDisponiveis = estacoesDisponiveis;
    }

    // Getters e Setters
    public EntSala getSala() { return sala; }
    public void setSala(EntSala sala) { this.sala = sala; }
    public List<EntEstacao> getEstacoesDisponiveis() { return estacoesDisponiveis; }
    public void setEstacoesDisponiveis(List<EntEstacao> estacoesDisponiveis) { this.estacoesDisponiveis = estacoesDisponiveis; }
}