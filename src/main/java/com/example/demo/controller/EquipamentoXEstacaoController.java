package com.example.demo.controller;

import com.example.demo.dto.EquipamentoXEstacaoDTO;
import com.example.demo.service.EquipamentoXEstacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipamento-estacao")
public class EquipamentoXEstacaoController {

    @Autowired
    private EquipamentoXEstacaoService service;

    @PostMapping
    public ResponseEntity<EquipamentoXEstacaoDTO> adicionar(@RequestBody EquipamentoXEstacaoDTO dto) {
        return ResponseEntity.ok(service.adicionar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoXEstacaoDTO> editar(@PathVariable Long id, @RequestBody EquipamentoXEstacaoDTO dto) {
        return ResponseEntity.ok(service.editar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.ok("A relação com ID " + id + " excluída com sucesso!");
    }

    @GetMapping
    public ResponseEntity<List<EquipamentoXEstacaoDTO>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/estacao/{idestacao}")
    public ResponseEntity<List<EquipamentoXEstacaoDTO>> buscarPorEstacao(@PathVariable Long idestacao) {
        return ResponseEntity.ok(service.buscarPorEstacao(idestacao));
    }

    @GetMapping("/equipamento/{idequipamento}")
    public ResponseEntity<List<EquipamentoXEstacaoDTO>> buscarPorEquipamento(@PathVariable Long idequipamento) {
        return ResponseEntity.ok(service.buscarPorEquipamento(idequipamento));
    }
}
