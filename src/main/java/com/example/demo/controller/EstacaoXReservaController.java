package com.example.demo.controller;

import com.example.demo.dto.EstacaoXReservaDTO;
import com.example.demo.service.EstacaoXReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estacao-reserva")
public class EstacaoXReservaController {

    @Autowired
    private EstacaoXReservaService service;

    // 1. Adicionar (Inserir)
    @PostMapping
    public ResponseEntity<EstacaoXReservaDTO> adicionar(@RequestBody EstacaoXReservaDTO dto) {
        return ResponseEntity.ok(service.inserir(dto));
    }

    // 2. Editar
    @PutMapping("/{id}")
    public ResponseEntity<EstacaoXReservaDTO> editar(@PathVariable Long id, @RequestBody EstacaoXReservaDTO dto) {
        return ResponseEntity.ok(service.editar(id, dto));
    }

    // 3. Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.ok("A relação com ID " + id + " excluída com sucesso!");
    }

    // 4. Listar Todos
    @GetMapping
    public ResponseEntity<List<EstacaoXReservaDTO>> listarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    // 5. Listar por Estação
    @GetMapping("/estacao/{idestacao}")
    public ResponseEntity<List<EstacaoXReservaDTO>> listarPorEstacao(@PathVariable Long idestacao) {
        return ResponseEntity.ok(service.buscarPorEstacao(idestacao));
    }

    // 6. Listar por Reserva
    @GetMapping("/reserva/{idreserva}")
    public ResponseEntity<List<EstacaoXReservaDTO>> listarPorReserva(@PathVariable Long idreserva) {
        return ResponseEntity.ok(service.buscarPorReserva(idreserva));
    }
}