package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTO que representa a requisição para reserva de sala por perfil de profissionais")
public class ReservaRequestDTO {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora de início da reserva", example = "2026-06-06T08:00:00", type = "string")
    private LocalDateTime dataInicio;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data e hora de fim da reserva", example = "2026-06-06T12:00:00", type = "string")
    private LocalDateTime dataFim;

    @Schema(description = "Quantidade de desenvolvedores", example = "0")
    private Long qtdDev;

    @Schema(description = "Quantidade de designers", example = "4")
    private Long qtdDesign;

    @Schema(description = "Quantidade de estações simples", example = "4")
    private Long qtdSimples;

    @Schema(description = "ID do usuário que está realizando a reserva", example = "5")
    private Long idUsuario;

    // --- GETTERS E SETTERS ---

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }
    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }
    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Long getQtdDev() {
        return qtdDev;
    }
    public void setQtdDev(Long qtdDev) {
        this.qtdDev = qtdDev;
    }

    public Long getQtdDesign() {
        return qtdDesign;
    }
    public void setQtdDesign(Long qtdDesign) {
        this.qtdDesign = qtdDesign;
    }

    public Long getQtdSimples() {
        return qtdSimples;
    }
    public void setQtdSimples(Long qtdSimples) {
        this.qtdSimples = qtdSimples;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}