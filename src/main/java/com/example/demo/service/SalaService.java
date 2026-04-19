package com.example.demo.service;


import com.example.demo.model.EntSala;
import com.example.demo.dto.SalaDTO; // Certifique- suitcase você criou este DTO
import com.example.demo.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    // 1. Adicionar Sala
    public SalaDTO adicionarSala(SalaDTO salaDTO) {
        EntSala entSala = EntSala.builder()
                .idsala(salaDTO.getIdsala())
                .endereco(salaDTO.getEndereco())
                .disponibilidade(salaDTO.getDisponibilidade())
                .lot_max(salaDTO.getLot_max())
                .build();

        EntSala sala = salaRepository.save(entSala);
        salaRepository.save(entSala);
        return mapToDTO(sala);
    }

    // 2. Remover Sala
    public void removerSala(Long idsala) {
        salaRepository.deleteById(idsala);
    }

    // 3. Buscar Sala por ID
    public SalaDTO buscarPorId(Long idsala) {
        EntSala sala = salaRepository.findById(idsala)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada com ID: " + idsala));
        return mapToDTO(sala);
    }

    // 4. Buscar por Disponibilidade
    public List<SalaDTO> buscarPorDisponibilidade(Boolean disponivel) {
        return salaRepository.findByDisponibilidade(disponivel).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 5. Buscar por Endereço
    public List<SalaDTO> buscarPorEndereco(String endereco) {
        return salaRepository.findByEnderecoContainingIgnoreCase(endereco).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 6. Buscar por Endereço e Disponibilidade
    public List<SalaDTO> buscarPorEnderecoEDisponibilidade(String endereco, Boolean disponivel) {
        return salaRepository.findByEnderecoContainingIgnoreCaseAndDisponibilidade(endereco, disponivel).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    // 7. Editar Sala
    public SalaDTO editarSala(SalaDTO salaDTO) {
        // Busca a sala existente ou lança erro se não encontrar
        EntSala salaExistente = salaRepository.findById(salaDTO.getIdsala())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada com ID: " + salaDTO.getIdsala()));

        // Atualiza os campos (exceto o ID que permanece o mesmo)
        salaExistente.setEndereco(salaDTO.getEndereco());
        salaExistente.setDisponibilidade(salaDTO.getDisponibilidade());
        salaExistente.setLot_max(salaDTO.getLot_max());

        // Salva as alterações
        EntSala salaAtualizada = salaRepository.save(salaExistente);

        return mapToDTO(salaAtualizada);
    }
    // NOVO: Listar todas as salas
    public List<SalaDTO> listarTodas() {
        return salaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    // Método Auxiliar de Mapeamento (Seguindo o seu padrão do ReservaService)
    private SalaDTO mapToDTO(EntSala sala) {
        return SalaDTO.builder()
                .idsala(sala.getIdsala())
                .endereco(sala.getEndereco())
                .disponibilidade(sala.getDisponibilidade())
                .lot_max(sala.getLot_max())
                .build();
    }
}
