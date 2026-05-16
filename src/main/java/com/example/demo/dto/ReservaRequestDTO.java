package com.example.demo.dto;

import java.time.LocalDateTime;

public class ReservaRequestDTO {

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Long qtdDev;
    private Long qtdDesign;
    private Long idUsuario; // ID do profissional que está fazendo a reserva

    // Getters e Setters (ou use a anotação @Data do Lombok)
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }

    public Long getQtdDev() { return qtdDev; }
    public void setQtdDev(Long qtdDev) { this.qtdDev = qtdDev; }

    public Long getQtdDesign() { return qtdDesign; }
    public void setQtdDesign(Long qtdDesign) { this.qtdDesign = qtdDesign; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}