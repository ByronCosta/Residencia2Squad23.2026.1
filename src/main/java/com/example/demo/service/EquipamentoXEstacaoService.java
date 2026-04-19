package com.example.demo.service;

import com.example.demo.dto.EquipamentoXEstacaoDTO;
import com.example.demo.model.EntEquipamentoXEstacao;
import com.example.demo.repository.EquipamentoXEstacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipamentoXEstacaoService {

    private final EquipamentoXEstacaoRepository repository;

    // 1. Adicionar (Vincular equipamento à estação)
    public EquipamentoXEstacaoDTO adicionar(EquipamentoXEstacaoDTO dto) {
        EntEquipamentoXEstacao entidade = EntEquipamentoXEstacao.builder()

                .idequipamento(dto.getIdequipamento())
                .idestacao(dto.getIdestacao())
                .build();
        EntEquipamentoXEstacao entidadeSalva = repository.save(entidade);
        return mapToDTO(repository.save(entidadeSalva));
    }

    // 2. Editar (Alterar um vínculo existente)
    public EquipamentoXEstacaoDTO editar(Long id, EquipamentoXEstacaoDTO dto) {
        EntEquipamentoXEstacao existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado"));

        existente.setIdequipamento(dto.getIdequipamento());
        existente.setIdestacao(dto.getIdestacao());

        return mapToDTO(repository.save(existente));
    }

    // 3. Remover por ID Geral
    public void remover(Long id) {
        repository.deleteById(id);
    }

    // 4. Buscar por Equipamento
    public List<EquipamentoXEstacaoDTO> buscarPorEquipamento(Long idequipamento) {
        return repository.findByIdequipamento(idequipamento).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 5. Buscar por Estação
    public List<EquipamentoXEstacaoDTO> buscarPorEstacao(Long idestacao) {
        return repository.findByIdestacao(idestacao).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 6. Buscar Todos
    public List<EquipamentoXEstacaoDTO> buscarTodos() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 7. Remover por Equipamento (Limpa todos os vínculos de um equipamento específico)
    public void removerPorEquipamento(Long idequipamento) {
        List<EntEquipamentoXEstacao> vinculos = repository.findByIdequipamento(idequipamento);
        repository.deleteAll(vinculos);
    }

    // 8. Remover por Estação (Limpa todos os equipamentos de uma estação específica)
    public void removerPorEstacao(Long idestacao) {
        List<EntEquipamentoXEstacao> vinculos = repository.findByIdestacao(idestacao);
        repository.deleteAll(vinculos);
    }

    // Método Auxiliar de Mapeamento
    private EquipamentoXEstacaoDTO mapToDTO(EntEquipamentoXEstacao entidade) {
        return EquipamentoXEstacaoDTO.builder()
                .id(entidade.getId()) // Garanta que o DTO tenha o campo 'id'
                .idequipamento(entidade.getIdequipamento())
                .idestacao(entidade.getIdestacao())
                .build();
    }
}