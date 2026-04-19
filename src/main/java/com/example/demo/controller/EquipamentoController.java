package com.example.demo.controller;

import com.example.demo.dto.EquipamentoDTO;
import com.example.demo.service.EquipamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipamentos")
public class EquipamentoController {

    @Autowired
    private EquipamentoService equipamentoService;

    // 1. Adicionar novo Equipamento (POST)
    @PostMapping
    public ResponseEntity<EquipamentoDTO> adicionar(@RequestBody EquipamentoDTO equipamentoDTO) {
        return ResponseEntity.ok(equipamentoService.adicionarEquipamento(equipamentoDTO));
    }

    // 2. Listar todos os Equipamentos (GET)
    @GetMapping
    public ResponseEntity<List<EquipamentoDTO>> listarTodos() {
        return ResponseEntity.ok(equipamentoService.listarTodas());
    }

    // 3. Buscar Equipamento por ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(equipamentoService.buscarPorId(id));
    }

    // 4. Buscar por Status de Estoque (GET)
    // Exemplo: /equipamentos/estoque?disponivel=true
    @GetMapping("/estoque")
    public ResponseEntity<List<EquipamentoDTO>> buscarPorEstoque(@RequestParam Boolean disponivel) {
        return ResponseEntity.ok(equipamentoService.buscarPorEstoque(disponivel));
    }

    // 5. Editar Equipamento (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoDTO> editar(@PathVariable Long id, @RequestBody EquipamentoDTO equipamentoDTO) {
        equipamentoDTO.setIdequipamento(id);
        return ResponseEntity.ok(equipamentoService.editarEquipamento(equipamentoDTO));
    }

    // 6. Deletar Equipamento (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        equipamentoService.removerEquipamento(id);
        return ResponseEntity.ok("O equipamento com ID " + id + " excluído com sucesso!");
    }
}
