package com.example.demo.dto;

import java.util.List;

public class ReservaComEstacoesResponseDTO {
    private List<ReservaDTO> reservas;
    private List<EstacaoCoordenadasDTO> estacoes;

    public ReservaComEstacoesResponseDTO(List<ReservaDTO> reservas, List<EstacaoCoordenadasDTO> estacoes) {
        this.reservas = reservas;
        this.estacoes = estacoes;
    }

    // Getters e Setters (ou use @Data do Lombok)
    public List<ReservaDTO> getReservas() { return reservas; }
    public void setReservas(List<ReservaDTO> reservas) { this.reservas = reservas; }
    public List<EstacaoCoordenadasDTO> getEstacoes() { return estacoes; }
    public void setEstacoes(List<EstacaoCoordenadasDTO> estacoes) { this.estacoes = estacoes; }
}