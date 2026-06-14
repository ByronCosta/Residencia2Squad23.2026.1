package com.example.demo.controller;

import com.example.demo.model.EntProfissional;
import com.example.demo.model.ProfissionalDTO;
import com.example.demo.service.ProfissionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profissionais")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ProfissionalController {

    @Autowired
    private ProfissionalService profissionalService;

    // --- ADICIONAR ---
    @PostMapping
    public ResponseEntity<ProfissionalDTO> adicionar(@RequestBody EntProfissional profissional) {
        return ResponseEntity.ok(profissionalService.salvar(profissional));
    }

    // --- BUSCAR TODOS ---
    @GetMapping
    public ResponseEntity<List<ProfissionalDTO>> buscarTodos() {
        return ResponseEntity.ok(profissionalService.listarTodos());
    }

    // --- BUSCAR POR ID ---
    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profissionalService.buscarPorId(id));
    }

    // --- EDITAR ---
    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> editar(@PathVariable Long id, @RequestBody EntProfissional profissional) {
        return ResponseEntity.ok(profissionalService.editar(id, profissional));
    }

    // --- DELETAR ---
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        profissionalService.deletar(id);
        return ResponseEntity.ok("Profissional com ID " + id + " excluído com sucesso!");    }
}