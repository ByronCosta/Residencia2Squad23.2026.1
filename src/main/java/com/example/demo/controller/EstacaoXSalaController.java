package com.example.demo.controller;

import com.example.demo.dto.EstacaoXSalaDTO;
import com.example.demo.service.EstacaoXSalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estacao-sala")
public class EstacaoXSalaController {

    @Autowired
    private EstacaoXSalaService service;

    // 1. Adicionar Vínculo
    @PostMapping
    public ResponseEntity<EstacaoXSalaDTO> adicionar(@RequestBody EstacaoXSalaDTO dto) {
        return ResponseEntity.ok(service.adicionar(dto));
    }

    // 2. Editar Vínculo
    @PutMapping("/{id}")
    public ResponseEntity<EstacaoXSalaDTO> editar(@PathVariable Long id, @RequestBody EstacaoXSalaDTO dto) {
        return ResponseEntity.ok(service.editar(id, dto));
    }

    // 3. Excluir Vínculo
    @DeleteMapping("/{id}")
    public ResponseEntity<String> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.ok("A relação com ID " + id + " excluída com sucesso!");
    }

    // 4. Buscar Todos os Vínculos
    @GetMapping
    public ResponseEntity<List<EstacaoXSalaDTO>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    // 5. Buscar por Estação
    @GetMapping("/estacao/{idestacao}")
    public ResponseEntity<List<EstacaoXSalaDTO>> buscarPorEstacao(@PathVariable Long idestacao) {
        return ResponseEntity.ok(service.buscarPorEstacao(idestacao));
    }

    // 6. Buscar por Sala
    @GetMapping("/sala/{idsala}")
    public ResponseEntity<List<EstacaoXSalaDTO>> buscarPorSala(@PathVariable Long idsala) {
        return ResponseEntity.ok(service.buscarPorSala(idsala));
    }
}
