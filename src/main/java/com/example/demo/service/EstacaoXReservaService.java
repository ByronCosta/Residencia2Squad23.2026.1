package com.example.demo.service;

import com.example.demo.dto.EstacaoXReservaDTO;
import com.example.demo.model.EntEstacaoXReserva;
import com.example.demo.repository.EstacaoXReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstacaoXReservaService {

    @Autowired
    private EstacaoXReservaRepository repository;

    // 1. Inserir (Adicionar)
    public EstacaoXReservaDTO inserir(EstacaoXReservaDTO dto) {
        EntEstacaoXReserva entidade = EntEstacaoXReserva.builder()
                .idestacao(dto.getIdestacao())
                .idreserva(dto.getIdreserva())
                .build();

        // Salva e captura o retorno com o ID gerado
        EntEstacaoXReserva salva = repository.save(entidade);
        return mapToDTO(salva);
    }

    // 2. Editar
    public EstacaoXReservaDTO editar(Long id, EstacaoXReservaDTO dto) {
        EntEstacaoXReserva existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vínculo de reserva não encontrado"));

        existente.setIdestacao(dto.getIdestacao());
        existente.setIdreserva(dto.getIdreserva());

        return mapToDTO(repository.save(existente));
    }

    // 3. Deletar (Remover)
    public void deletar(Long id) {
        repository.deleteById(id);
    }

    // 4. Buscar Todos
    public List<EstacaoXReservaDTO> buscarTodos() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 5. Buscar por Estação
    public List<EstacaoXReservaDTO> buscarPorEstacao(Long idestacao) {
        return repository.findByIdestacao(idestacao).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 6. Buscar por Reserva
    public List<EstacaoXReservaDTO> buscarPorReserva(Long idreserva) {
        return repository.findByIdreserva(idreserva).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Método Auxiliar de Mapeamento
    private EstacaoXReservaDTO mapToDTO(EntEstacaoXReserva entidade) {
        return EstacaoXReservaDTO.builder()
                .id(entidade.getId()) // Certifique-se que o campo na Entity e DTO se chama 'id'
                .idestacao(entidade.getIdestacao())
                .idreserva(entidade.getIdreserva())
                .build();
    }
}