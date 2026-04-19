package com.example.demo.service;

import com.example.demo.dto.EstacaoXSalaDTO;
import com.example.demo.model.EntEstacaoXSala;
import com.example.demo.repository.EstacaoXSalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstacaoXSalaService {

    @Autowired
    private EstacaoXSalaRepository repository;

    // 1. Adicionar
    public EstacaoXSalaDTO adicionar(EstacaoXSalaDTO dto) {
        EntEstacaoXSala entidade = EntEstacaoXSala.builder()
                .idestacao(dto.getIdestacao())
                .idsala(dto.getIdsala())
                .build();

        // Salva e recupera a entidade com o ID gerado pelo banco
        EntEstacaoXSala salva = repository.save(entidade);
        return mapToDTO(salva);
    }

    // 2. Editar
    public EstacaoXSalaDTO editar(Long id, EstacaoXSalaDTO dto) {
        EntEstacaoXSala existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado"));

        existente.setIdestacao(dto.getIdestacao());
        existente.setIdsala(dto.getIdsala());

        return mapToDTO(repository.save(existente));
    }

    // 3. Remover
    public void remover(Long id) {
        repository.deleteById(id);
    }

    // 4. Buscar Todos
    public List<EstacaoXSalaDTO> buscarTodos() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 5. Buscar por Estação
    public List<EstacaoXSalaDTO> buscarPorEstacao(Long idestacao) {
        return repository.findByIdestacao(idestacao).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 6. Buscar por Sala
    public List<EstacaoXSalaDTO> buscarPorSala(Long idsala) {
        return repository.findByIdsala(idsala).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Método Auxiliar de Mapeamento
    private EstacaoXSalaDTO mapToDTO(EntEstacaoXSala entidade) {
        return EstacaoXSalaDTO.builder()
                .id(entidade.getId()) // Certifique-se que o campo na Entity se chama 'id'
                .idestacao(entidade.getIdestacao())
                .idsala(entidade.getIdsala())
                .build();
    }
}
