package com.example.demo.controller;

import com.example.demo.dto.ReservaRequestDTO;
import com.example.demo.dto.SalaDTO;
import com.example.demo.dto.SalasEEstacoesDisponiveisDTO;
import com.example.demo.model.EntEquipamento;
import com.example.demo.model.EntEstacao;
import com.example.demo.model.EntSala;
import com.example.demo.repository.SalaRepository;
import com.example.demo.service.GeminiWorkspaceService;
import com.example.demo.service.ReservaService;
import com.example.demo.service.SalaService;
import com.example.demo.repository.EquipamentoRepository;
import com.example.demo.repository.EstacaoRepository;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/salas")
public class SalaController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private EstacaoRepository estacaoRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private ReservaService reservaService;

    private final String FASTAPI_URL = "http://0.0.0.0:8000/analisar";//"http://127.0.0.1:8000/analisar"
    private final GeminiWorkspaceService geminiService = new GeminiWorkspaceService();
    // Adicionar imagem e integrar com FastAPI
    @PostMapping("/{id}/upload-planta")
    public ResponseEntity<?> fazerUploadPlanta(
            @PathVariable("id") Long idSala,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um arquivo JPG.");
        }

        if (!"image/jpeg".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Apenas arquivos JPG/JPEG são suportados.");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            org.springframework.core.io.ByteArrayResource fileAsResource =
                    new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };

            body.add("file", fileAsResource);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    FASTAPI_URL,
                    requestEntity,
                    JsonNode.class
            );

            JsonNode estacoesArray = response.getBody();

            if (estacoesArray != null && estacoesArray.isArray()) {
                for (JsonNode noEstacao : estacoesArray) {

                    int numeroEstacao = noEstacao.get("estacao").asInt();
                    int coordX = noEstacao.get("coordx").asInt();
                    int coordY = noEstacao.get("coordy").asInt();

                    JsonNode itens = noEstacao.get("itens");
                    int qtdCadeiras = itens.has("cadeira") ? itens.get("cadeira").asInt() : 0;
                    int qtdMonitores = itens.has("monitor") ? itens.get("monitor").asInt() : 0;

                    String descricaoEstacao;
                    if (qtdMonitores == 1) {
                        descricaoEstacao = "dev";
                    } else if (qtdMonitores >= 2) {
                        descricaoEstacao = "design";
                    } else {
                        descricaoEstacao = "simples";
                    }

                    EntEstacao estacao = new EntEstacao();
                    estacao.setDescricao(descricaoEstacao);
                    estacao.setCoordx(coordX);
                    estacao.setCoordy(coordY);
                    estacao.setIdsala(idSala);

                    EntEstacao estacaoSalva = estacaoRepository.save(estacao);
                    Long idEstacaoGerado = estacaoSalva.getIdestacao();

                    for (int i = 0; i < qtdCadeiras; i++) {
                        EntEquipamento cadeira = EntEquipamento.builder()
                                .idestacao(idEstacaoGerado)
                                .descricao("Cadeira da Estação " + numeroEstacao + " (" + descricaoEstacao + ")")
                                .estoque(false)
                                .build();
                        equipamentoRepository.save(cadeira);
                    }

                    for (int i = 0; i < qtdMonitores; i++) {
                        EntEquipamento monitor = EntEquipamento.builder()
                                .idestacao(idEstacaoGerado)
                                .descricao("Monitor da Estação " + numeroEstacao + " (" + descricaoEstacao + ")")
                                .estoque(false)
                                .build();
                        equipamentoRepository.save(monitor);
                    }
                }

                // --- REGRA DE NEGÓCIO ADICIONADA AQUI ---
                // 1. Conta o total de estações salvas que possuem o idsala correspondente
                int totalEstacoes = estacaoRepository.countByIdsala(idSala);

                // 2. Busca a sala correspondente no banco de dados
                Optional<EntSala> salaOptional = salaRepository.findById(idSala);

                if (salaOptional.isPresent()) {
                    EntSala sala = salaOptional.get();
                    // 3. Atualiza a lotação máxima com o total de estações
                    sala.setLot_max(totalEstacoes);
                    // 4. Salva a sala atualizada
                    salaRepository.save(sala);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Estações salvas, mas a sala com ID " + idSala + " não foi encontrada para atualizar a lotação.");
                }
                // ----------------------------------------
            }

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Planta processada. Estações e equipamentos salvos. Lotação máxima da sala " + idSala + " atualizada para " + estacaoRepository.countByIdsala(idSala)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar a imagem ou salvar no banco: " + e.getMessage());
        }
    }
    @PostMapping("/importar")
    public ResponseEntity<?> importarDadosIA(@RequestBody Map<String, Object> dados) {
        System.out.println("Dados da IA recebidos: " + dados);
        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Dados importados"));
    }

    @PostMapping("/processar-ia")
    public ResponseEntity<?> receberDadosIA(@RequestBody Map<String, Object> dadosIA) {
        System.out.println("Dados recebidos da IA: " + dadosIA);
        return ResponseEntity.ok(Map.of("status", "recebido com sucesso"));
    }

    // 1. Adicionar nova sala
    @PostMapping
    public ResponseEntity<SalaDTO> adicionar(@RequestBody SalaDTO salaDTO) {
        return ResponseEntity.ok(salaService.adicionarSala(salaDTO));
    }

    // 2. Listar todas as salas
    @GetMapping
    public ResponseEntity<List<SalaDTO>> listarTodas() {
        return ResponseEntity.ok(salaService.listarTodas());
    }

    // 3. Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<SalaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(salaService.buscarPorId(id));
    }

    // 4. Buscar por Disponibilidade
    @GetMapping("/disponivel")
    public ResponseEntity<List<SalaDTO>> buscarPorDisponibilidade(@RequestParam Boolean disponivel) {
        return ResponseEntity.ok(salaService.buscarPorDisponibilidade(disponivel));
    }

    // 5. Buscar por Endereço (Parte do texto)
    @GetMapping("/buscar-endereco")
    public ResponseEntity<List<SalaDTO>> buscarPorEndereco(@RequestParam String address) {
        return ResponseEntity.ok(salaService.buscarPorEndereco(address));
    }

    // 6. Buscar por Endereço e Disponibilidade
    @GetMapping("/buscar-filtro")
    public ResponseEntity<List<SalaDTO>> buscarPorEnderecoEDisponibilidade(
            @RequestParam String endereco,
            @RequestParam Boolean disponivel) {
        return ResponseEntity.ok(salaService.buscarPorEnderecoEDisponibilidade(endereco, disponivel));
    }

    // 7. Buscar salas pela quantidade mínima de estações Dev e Design
    @GetMapping("/buscar-por-perfis")
    public ResponseEntity<List<SalaDTO>> buscarPorCapacidadeDePerfis(
            @RequestParam Long qtdDev,
            @RequestParam Long qtdDesign) {
        List<SalaDTO> salasValidas = salaService.buscarSalasPorCapacidadeDePerfis(qtdDev, qtdDesign);
        return ResponseEntity.ok(salasValidas);
    }

    // 8. Editar Sala
    @PutMapping("/{id}")
    public ResponseEntity<SalaDTO> editar(@PathVariable Long id, @RequestBody SalaDTO salaDTO) {
        salaDTO.setIdsala(id);
        return ResponseEntity.ok(salaService.editarSala(salaDTO));
    }

    // 9. Deletar Sala
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        salaService.removerSala(id);
        return ResponseEntity.ok("A Sala com ID " + id + " excluída com sucesso!");
    }

    @PostMapping("gemini/sugerir-estacoes")
    public ResponseEntity<?> sugerirEstacoesParaEquipe(@RequestBody ReservaRequestDTO perfilDTO) {
        try {
            List<SalasEEstacoesDisponiveisDTO> salasDisponiveis =
                    reservaService.consultarTodasSalasEEstacoesLivres(perfilDTO);

            if (salasDisponiveis.isEmpty()) {
                return ResponseEntity.ok("Nenhuma estação disponível para o período informado.");
            }

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