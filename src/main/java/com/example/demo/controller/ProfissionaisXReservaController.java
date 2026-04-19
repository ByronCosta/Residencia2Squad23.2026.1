package com.example.demo.controller;

import com.example.demo.dto.ProfissionaisXReservaDTO;
import com.example.demo.service.ProfissionaisXReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/profissionais-reserva")
public class ProfissionaisXReservaController {

    @Autowired
    private ProfissionaisXReservaService service;

    @PostMapping
    public ResponseEntity<ProfissionaisXReservaDTO> adicionar(@RequestBody ProfissionaisXReservaDTO dto) {
        return ResponseEntity.ok(service.adicionar(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProfissionaisXReservaDTO>> listarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }

    @GetMapping("/reserva/{idreserva}")
    public ResponseEntity<List<ProfissionaisXReservaDTO>> listarPorReserva(@PathVariable Long idreserva) {
        return ResponseEntity.ok(service.buscarPorReserva(idreserva));
    }

    // Corrigido para /profissionais-reserva/reserva/{id}
    @PutMapping("/reserva/{id}")
    public ResponseEntity<ProfissionaisXReservaDTO> editar(@PathVariable Long id, @RequestBody ProfissionaisXReservaDTO dto) {
        return ResponseEntity.ok(service.editar(id, dto));
    }

    // CORREÇÃO AQUI: Adicionado "/reserva" para aceitar DELETE /profissionais-reserva/reserva/{id}
    @DeleteMapping("/reserva/{id}")
    public ResponseEntity<String> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.ok("A relação com ID " + id + " excluída com sucesso!");
    }
}