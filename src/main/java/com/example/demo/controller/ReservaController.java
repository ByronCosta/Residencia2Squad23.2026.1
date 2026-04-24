package com.example.demo.controller;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    // --- MÉTODOS CRUD BÁSICOS ---

    @PostMapping
    public ResponseEntity<?> adicionar(@RequestBody ReservaDTO reservaDTO) {
        try {
            // Tenta adicionar a reserva executando as validações do Service
            ReservaDTO novaReserva = reservaService.adicionarReserva(reservaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva);
        } catch (RuntimeException e) {
            // Se cair aqui, é porque a validação de lotação ou disponibilidade falhou
            // Retorna 400 (Bad Request) com a mensagem personalizada
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> buscarTodas() {
        return ResponseEntity.ok(reservaService.buscarTodasReservas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> buscarPorId(@PathVariable Long id) {
        ReservaDTO dto = new ReservaDTO();
        dto.setIdreserva(id);
        return ResponseEntity.ok(reservaService.buscarReservaPorId(dto));
    }

    @PutMapping
    public ResponseEntity<?> editar(@RequestBody ReservaDTO reservaDTO) {
        try {
            return ResponseEntity.ok(reservaService.editarReserva(reservaDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaDTO> deletar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.deletarReserva(id));
    }

    // --- NOVOS MÉTODOS DE BUSCA (FILTROS) ---

    @GetMapping("/sala/{idsala}")
    public ResponseEntity<List<ReservaDTO>> buscarPorSala(@PathVariable Long idsala) {
        return ResponseEntity.ok(reservaService.buscarPorSala(idsala));
    }

    @GetMapping("/usuario/{idusuario}")
    public ResponseEntity<List<ReservaDTO>> buscarPorUsuario(@PathVariable Long idusuario) {
        return ResponseEntity.ok(reservaService.buscarPorUsuario(idusuario));
    }

    @GetMapping("/profissional/{idprofissional}")
    public ResponseEntity<List<ReservaDTO>> buscarPorProfissional(@PathVariable Long idprofissional) {
        return ResponseEntity.ok(reservaService.buscarPorProfissional(idprofissional));
    }


}