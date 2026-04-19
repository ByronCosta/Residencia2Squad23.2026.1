package com.example.demo.controller;

import com.example.demo.dto.SalaDTO;
import com.example.demo.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaController {

    @Autowired
    private SalaService salaService;

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