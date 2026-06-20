package com.example.demo.controller;

import com.example.demo.dto.ReservaRequestDTO;
import com.example.demo.dto.SalasEEstacoesDisponiveisDTO;
import com.example.demo.service.GeminiWorkspaceService;
import com.example.demo.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/api/workspace")
public class WorkspaceController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private GeminiWorkspaceService geminiService;

    /**
     * Recebe um ReservaRequestDTO com período e quantidades de perfis,
     * busca todas as estações disponíveis no banco e envia ao Gemini
     * para selecionar as mais próximas entre si.
     *
     * POST http://localhost:8080/api/workspace/sugerir-estacoes
     *
     * Body de exemplo:
     * {
     *   "dataInicio": "2026-06-10T09:00:00",
     *   "dataFim":    "2026-06-10T18:00:00",
     *   "qtdDev":     4,
     *   "qtdDesign":  5,
     *   "qtdSimples": 1,
     *   "idUsuario":  1
     * }
     */
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN', 'USER')")
    @PostMapping("/sugerir-estacoes")
    public ResponseEntity<?> sugerirEstacoesParaEquipe(
            @RequestBody ReservaRequestDTO perfilDTO) {
        try {
            // 1. Busca todas as salas e estações disponíveis no período informado
            //    Reutiliza o método já existente no ReservaService
            List<SalasEEstacoesDisponiveisDTO> salasDisponiveis =
                    reservaService.consultarTodasSalasEEstacoesLivres(perfilDTO);

            if (salasDisponiveis.isEmpty()) {
                return ResponseEntity.ok("Nenhuma estação disponível para o período informado.");
            }

            // 2. Envia ao Gemini para selecionar as estações mais próximas
            List<SalasEEstacoesDisponiveisDTO> sugestao = geminiService.selecionarEstacoesParaEquipe(
                    salasDisponiveis,
                    perfilDTO.getQtdDev().intValue(),
                    perfilDTO.getQtdDesign().intValue(),
                    perfilDTO.getQtdSimples().intValue()
            );

            return ResponseEntity.ok(sugestao);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao consultar a IA: " + e.getMessage());
        }
    }
}
