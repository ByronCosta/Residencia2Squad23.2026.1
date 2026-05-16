package com.example.demo.controller;

import com.example.demo.dto.SalaDTO;
import com.example.demo.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/salas")
public class SalaController {

    @Autowired
    private SalaService salaService;
    private final String FASTAPI_URL = "http://127.0.0.1:8000/analisar";

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

            ResponseEntity<String> response = restTemplate.postForEntity(
                    FASTAPI_URL,
                    requestEntity,
                    String.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar a imagem com a IA: " + e.getMessage());
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
    public ResponseEntity<List<SalaDTO>> buscarPorEndereco(@RequestParam String endereco) {
        return ResponseEntity.ok(salaService.buscarPorEndereco(endereco));
    }

    // 6. Buscar por Endereço e Disponibilidade
    @GetMapping("/buscar-filtro")
    public ResponseEntity<List<SalaDTO>> buscarPorEnderecoEDisponibilidade(
            @RequestParam String endereco,
            @RequestParam Boolean disponivel) {
        return ResponseEntity.ok(salaService.buscarPorEnderecoEDisponibilidade(endereco, disponivel));
    }

    // 7. Buscar salas pela quantidade mínima de estações Dev e Design (NOVO)
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
}