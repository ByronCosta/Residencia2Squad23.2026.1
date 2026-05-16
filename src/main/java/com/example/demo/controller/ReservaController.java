package com.example.demo.controller;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime; // Importação correta
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
            ReservaDTO novaReserva = reservaService.adicionarReserva(reservaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> buscarTodas() {
        return ResponseEntity.ok(reservaService.buscarTodasReservas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarReservaPorId(id));
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

    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    @PostMapping("/grupo")
    public ResponseEntity<List<ReservaDTO>> reservarEmGrupo(
            @RequestBody ReservaDTO baseDTO,
            @RequestParam int totalPessoas,
            @RequestParam Long idEstacaoReferencia) {
        List<ReservaDTO> reservas = reservaService.adicionarReservaEmGrupo(baseDTO, totalPessoas, idEstacaoReferencia);
        return ResponseEntity.ok(reservas);
    }

    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    @PostMapping("/separadas")
    public ResponseEntity<List<ReservaDTO>> reservarSeparadas(
            @RequestBody ReservaDTO baseDTO,
            @RequestParam int totalPessoas,
            @RequestParam int salto) {
        List<ReservaDTO> reservas = reservaService.adicionarReservaSeparada(baseDTO, totalPessoas, salto);
        return ResponseEntity.ok(reservas);
    }

    // --- CORREÇÃO DO MÉTODO VALIDAR-VAGA ---
    @GetMapping("/validar-vaga")
    public ResponseEntity<?> validarVaga(
            @RequestParam Long idsala,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicial,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFinal) {

        try {
            // Monta o DTO temporário para validação
            ReservaDTO tempDTO = ReservaDTO.builder()
                    .idsala(idsala)
                    .datainicial(dataInicial)
                    .datafinal(dataFinal)
                    .horainicial(horaInicial)
                    .horafinal(horaFinal)
                    .build();

            reservaService.validarDisponibilidade(tempDTO);
            return ResponseEntity.ok(true);

        } catch (RuntimeException e) {
            // Retorna o conflito com a mensagem da lotação
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}