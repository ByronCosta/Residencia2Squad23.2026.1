package com.example.demo.controller;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<ReservaDTO> adicionar(@RequestBody ReservaDTO reservaDTO) {
        return ResponseEntity.ok(reservaService.adicionarReserva(reservaDTO));
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
    public ResponseEntity<ReservaDTO> editar(@RequestBody ReservaDTO reservaDTO) {
        return ResponseEntity.ok(reservaService.editarReserva(reservaDTO));
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

    @GetMapping("/data-inicial")
    public ResponseEntity<List<ReservaDTO>> buscarPorDataInicial(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(reservaService.buscarPorDataInicial(data));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<ReservaDTO>> buscarEntreDatas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(reservaService.buscarEntreDatas(inicio, fim));
    }

}