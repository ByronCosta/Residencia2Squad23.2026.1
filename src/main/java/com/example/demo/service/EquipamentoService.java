package com.example.demo.service;

import com.example.demo.dto.EquipamentoDTO;
import com.example.demo.model.EntEquipamento;
import com.example.demo.repository.EquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipamentoService {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    // 1. Adicionar Equipamento
    public EquipamentoDTO adicionarEquipamento(EquipamentoDTO equipamentoDTO) {
        EntEquipamento entEquipamento = EntEquipamento.builder()
                .idequipamento(equipamentoDTO.getIdequipamento())
                .descricao(equipamentoDTO.getDescricao())
                .estoque(equipamentoDTO.getEstoque())
                .build();

        // Corrigido: Apenas um save é necessário
        EntEquipamento equipamentoSalvo = equipamentoRepository.save(entEquipamento);
        return mapToDTO(equipamentoSalvo);
    }

    // 2. Editar Equipamento (NOVO)
    public EquipamentoDTO editarEquipamento(EquipamentoDTO equipamentoDTO) {
        // Busca o equipamento existente para garantir que ele existe
        EntEquipamento equipamentoExistente = equipamentoRepository.findById(equipamentoDTO.getIdequipamento())
                .orElseThrow(() -> new RuntimeException("Equipamento não encontrado com ID: " + equipamentoDTO.getIdequipamento()));

        // Atualiza os campos
        equipamentoExistente.setDescricao(equipamentoDTO.getDescricao());
        equipamentoExistente.setEstoque(equipamentoDTO.getEstoque());

        // Salva as alterações
        EntEquipamento equipamentoAtualizado = equipamentoRepository.save(equipamentoExistente);
        return mapToDTO(equipamentoAtualizado);
    }

    // 3. Remover Equipamento (Deletar)
    public void removerEquipamento(Long idequipamento) {
        if (!equipamentoRepository.existsById(idequipamento)) {
            throw new RuntimeException("Não é possível remover: Equipamento não encontrado com ID: " + idequipamento);
        }
        equipamentoRepository.deleteById(idequipamento);
    }

    // 4. Buscar por ID
    public EquipamentoDTO buscarPorId(Long idequipamento) {
        EntEquipamento equipamento = equipamentoRepository.findById(idequipamento)
                .orElseThrow(() -> new RuntimeException("Equipamento não encontrado com ID: " + idequipamento));
        return mapToDTO(equipamento);
    }

    // 5. Buscar por Status de Estoque (Booleano)
    public List<EquipamentoDTO> buscarPorEstoque(Boolean temEstoque) {
        return equipamentoRepository.findByEstoque(temEstoque)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 6. Listar Todos
    public List<EquipamentoDTO> listarTodas() {
        return equipamentoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Método Auxiliar de Mapeamento
    private EquipamentoDTO mapToDTO(EntEquipamento equipamento) {
        return EquipamentoDTO.builder()
                .idequipamento(equipamento.getIdequipamento())
                .descricao(equipamento.getDescricao())
                .estoque(equipamento.getEstoque())
                .build();
    }
}