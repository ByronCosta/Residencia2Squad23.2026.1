package com.example.demo.controller;

import com.example.demo.dto.EstacaoDTO;
import com.example.demo.service.EstacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estacoes")
public class EstacaoController {

    @Autowired
    private EstacaoService estacaoService;

    // 1. Adicionar nova Estação (POST)
    @PostMapping
    public ResponseEntity<EstacaoDTO> adicionar(@RequestBody EstacaoDTO estacaoDTO) {
        return ResponseEntity.ok(estacaoService.adicionarEstacao(estacaoDTO));
    }

    // 2. Listar todas as Estações (GET)
    @GetMapping
    public ResponseEntity<List<EstacaoDTO>> listarTodas() {
        return ResponseEntity.ok(estacaoService.listarTodos());
    }

    // 3. Buscar Estação por ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<EstacaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(estacaoService.buscarPorId(id));
    }

    // 4. Editar Estação (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<EstacaoDTO> editar(@PathVariable Long id, @RequestBody EstacaoDTO estacaoDTO) {
        // Garante que o ID da URL seja o mesmo que será processado no Service
        estacaoDTO.setIdestacao(id);
        return ResponseEntity.ok(estacaoService.editarEstacao(estacaoDTO));
    }

    // 5. Deletar Estação (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        estacaoService.removerEstacao(id);
        return ResponseEntity.ok("A estação com ID " + id + " excluída com sucesso!");
    }
}