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
/*
    @PostMapping("/{id}/upload-planta")
    public ResponseEntity<?> fazerUploadPlanta(
            @PathVariable("id") Long idSala,
            @RequestParam("file") MultipartFile file) {

        // 1. Validação básica (Já está no seu código)
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um arquivo.");
        }

        if (!"image/jpeg".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Apenas arquivos JPG/JPEG são suportados.");
        }

        // TESTE: Apenas retornar os dados do arquivo para confirmar que o Spring leu
        String mensagemSucesso = String.format(
                "Arquivo recebido com sucesso! Nome: %s, Tamanho: %d bytes. Pronto para enviar para a sala ID: %d",
                file.getOriginalFilename(), file.getSize(), idSala
        );

        return ResponseEntity.ok(mensagemSucesso);
    }
    */
    //Adicionar imagem
    @PostMapping("/{id}/upload-planta")
    public ResponseEntity<?> fazerUploadPlanta(
            @PathVariable("id") Long idSala,
            @RequestParam("file") MultipartFile file) {

        // 1. Validação básica do arquivo
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecione um arquivo JPG.");
        }

        if (!"image/jpeg".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Apenas arquivos JPG/JPEG são suportados.");
        }

        try {
            // 2. Preparar a requisição multipart para a FastAPI
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Converter o MultipartFile recebido em um recurso que o RestTemplate consiga enviar
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            org.springframework.core.io.ByteArrayResource fileAsResource =
                    new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename(); // Importante para a FastAPI reconhecer o nome do arquivo
                        }
                    };

            body.add("file", fileAsResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 3. Chamar a FastAPI que roda a IA
            // O retorno da FastAPI será o JSON com as estações e equipamentos mapeados
            ResponseEntity<String> response = restTemplate.postForEntity(
                    FASTAPI_URL,
                    requestEntity,
                    String.class
            );

            // Aqui você pode adicionar lógica no Spring para salvar no banco 'reservasaccenture'
            // associando ao idSala se desejar, antes de retornar.

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar a imagem com a IA: " + e.getMessage());
        }


    }

    @PostMapping("/importar") // Precisa ser PostMapping e o caminho exato
    public ResponseEntity<?> importarDadosIA(@RequestBody Map<String, Object> dados) {
        System.out.println("Dados da IA recebidos: " + dados);

        // Aqui você faz a lógica para salvar no banco de dados

        return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Dados importados"));
    }

    @PostMapping("/processar-ia")
    public ResponseEntity<?> receberDadosIA(@RequestBody Map<String, Object> dadosIA) {
        // Aqui você recebe o que a IA detectou (itens, total_itens, etc)
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

    // 6. Editar Sala
    @PutMapping("/{id}")
    public ResponseEntity<SalaDTO> editar(@PathVariable Long id, @RequestBody SalaDTO salaDTO) {
        // É importante garantir que o ID do DTO seja o mesmo da URL antes de mandar para o Service
        salaDTO.setIdsala(id);
        return ResponseEntity.ok(salaService.editarSala(salaDTO));
    }

    // 7. Deletar Sala
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        salaService.removerSala(id);
        return ResponseEntity.ok("A Sala com ID " + id + " excluída com sucesso!");
    }
}