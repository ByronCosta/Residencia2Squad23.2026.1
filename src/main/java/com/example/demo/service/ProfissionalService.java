package com.example.demo.service;

import com.example.demo.model.EntProfissional;
import com.example.demo.model.ProfissionalDTO;
import com.example.demo.repository.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfissionalService {

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public ProfissionalDTO salvar(EntProfissional profissional) {
        profissional.setPassword(passwordEncoder.encode(profissional.getPassword()));
        EntProfissional salvo = profissionalRepository.save(profissional);
        return converterParaDTO(salvo);
    }

    // --- MÉTODO EDITAR ---
    @Transactional
    public ProfissionalDTO editar(Long id, EntProfissional dadosNovos) {
        EntProfissional profissionalExistente = profissionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado com ID: " + id));

        // Atualiza campos básicos (da classe User)
        profissionalExistente.setEmail(dadosNovos.getEmail());
        profissionalExistente.setRole(dadosNovos.getRole());

        // Atualiza o campo específico (da classe EntProfissional)
        profissionalExistente.setPerfil(dadosNovos.getPerfil());

        // Atualiza a senha apenas se ela for enviada e for diferente da atual
        if (dadosNovos.getPassword() != null && !dadosNovos.getPassword().isEmpty()) {
            profissionalExistente.setPassword(passwordEncoder.encode(dadosNovos.getPassword()));
        }

        EntProfissional atualizado = profissionalRepository.save(profissionalExistente);
        return converterParaDTO(atualizado);
    }

    // --- MÉTODO DELETAR ---
    @Transactional
    public void deletar(Long id) {
        if (!profissionalRepository.existsById(id)) {
            throw new RuntimeException("Não é possível deletar: Profissional não encontrado");
        }
        profissionalRepository.deleteById(id);
    }

    public List<ProfissionalDTO> listarTodos() {
        return profissionalRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public ProfissionalDTO buscarPorId(Long id) {
        EntProfissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        return converterParaDTO(profissional);
    }

    private ProfissionalDTO converterParaDTO(EntProfissional ent) {
        ProfissionalDTO dto = new ProfissionalDTO();
        dto.setId(ent.getId());
        dto.setEmail(ent.getEmail());
        dto.setRole(ent.getRole());
        dto.setPerfil(ent.getPerfil());
        return dto;
    }
}