package com.example.demo.controller;

import com.example.demo.dto.EstacaoXReservaDTO;
import com.example.demo.dto.ReservaRequestDTO; // Importado
import com.example.demo.model.EntReserva; // Importado
import com.example.demo.service.EstacaoXReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estacao-reserva")
@CrossOrigin(origins = "*") // Opcional: útil se o seu front em VueJS der erro de CORS
public class EstacaoXReservaController {

    @Autowired
    private EstacaoXReservaService service;

    // Novo Endpoint: Reserva em lote por quantidade de perfis
    /*@PostMapping("/por-perfis")
    public ResponseEntity<?> realizarReservaPorPerfis(@RequestBody ReservaRequestDTO dto) {
        try {
            EntReserva novaReserva = service.realizarReservaPorPerfis(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva);
        } catch (RuntimeException e) {
            // Retorna o texto do "throw new RuntimeException" do Service como erro 400
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
     */
    // 1. Adicionar (Inserir individual)
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