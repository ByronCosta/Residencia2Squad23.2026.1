package com.example.demo.service;

import com.example.demo.dto.ProfissionaisXReservaDTO;
import com.example.demo.model.EntProfissionaisXReserva;
import com.example.demo.repository.ProfissionaisXReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfissionaisXReservaService {

    @Autowired
    private ProfissionaisXReservaRepository repository;

    public ProfissionaisXReservaDTO adicionar(ProfissionaisXReservaDTO dto) {
        EntProfissionaisXReserva entidade = EntProfissionaisXReserva.builder()
                .idprofissional(dto.getIdprofissional())
                .idreserva(dto.getIdreserva())
                .build();

        return mapToDTO(repository.save(entidade));
    }

    public ProfissionaisXReservaDTO editar(Long id, ProfissionaisXReservaDTO dto) {
        // 1. Busca a entidade existente ou lança um erro se não encontrar
        EntProfissionaisXReserva existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado com o ID: " + id));

        // 2. Atualiza os campos (Exceto o ID da própria relação)
        existente.setIdprofissional(dto.getIdprofissional());
        existente.setIdreserva(dto.getIdreserva());

        // 3. Salva e retorna o DTO atualizado
        return mapToDTO(repository.save(existente));
    }
    public void remover(Long id) {
        repository.deleteById(id);
    }

    public List<ProfissionaisXReservaDTO> buscarTodos() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ProfissionaisXReservaDTO> buscarPorReserva(Long idreserva) {
        return repository.findByIdreserva(idreserva).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    private ProfissionaisXReservaDTO mapToDTO(EntProfissionaisXReserva entidade) {
        return ProfissionaisXReservaDTO.builder()
                .id(entidade.getId())
                .idprofissional(entidade.getIdprofissional())
                .idreserva(entidade.getIdreserva())
                .build();
    }
}