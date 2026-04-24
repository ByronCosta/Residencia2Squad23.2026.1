package com.example.demo.service;

import com.example.demo.dto.EstacaoXReservaDTO;
import com.example.demo.model.*;
import com.example.demo.repository.EstacaoXReservaRepository;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstacaoXReservaService {

    @Autowired
    private EstacaoXReservaRepository repository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Transactional
    public EstacaoXReservaDTO inserir(EstacaoXReservaDTO dto) {
        // Recupera os dados da Reserva pai
        EntReserva reserva = reservaRepository.findById(dto.getIdreserva())
                .orElseThrow(() -> new RuntimeException("Reserva pai não encontrada."));

        // Valida se a sala daquela reserva ainda tem espaço
        validarCapacidadeDisponivel(reserva);

        EntEstacaoXReserva entidade = EntEstacaoXReserva.builder()
                .idestacao(dto.getIdestacao())
                .idreserva(dto.getIdreserva())
                .build();

        EntEstacaoXReserva salva = repository.save(entidade);
        return mapToDTO(salva);
    }

    @Transactional
    public EstacaoXReservaDTO editar(Long id, EstacaoXReservaDTO dto) {
        EntEstacaoXReserva existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vínculo de reserva não encontrado"));

        // Se mudar a reserva, precisa validar a lotação na nova reserva selecionada
        if (!existente.getIdreserva().equals(dto.getIdreserva())) {
            EntReserva novaReserva = reservaRepository.findById(dto.getIdreserva())
                    .orElseThrow(() -> new RuntimeException("Nova reserva não encontrada."));
            validarCapacidadeDisponivel(novaReserva);
        }

        existente.setIdestacao(dto.getIdestacao());
        existente.setIdreserva(dto.getIdreserva());

        return mapToDTO(repository.save(existente));
    }

    /**
     * Valida se a sala comporta mais uma estação no período exato da reserva
     */
    private void validarCapacidadeDisponivel(EntReserva reserva) {
        EntSala sala = salaRepository.findById(reserva.getIdsala())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada."));

        // Chamada corrigida passando o idsala
        Long ocupacaoAtual = repository.contarEstacoesOcupadasNoPeriodo(
                reserva.getDatainicial(),
                reserva.getDatafinal(),
                reserva.getHorainicial(),
                reserva.getHorafinal()
        );

        // Verifica se a ocupação atual é maior ou igual ao limite permitido pela sala
        if (ocupacaoAtual >= sala.getLot_max()) {
            // Esta mensagem será capturada pelo seu GlobalExceptionHandler
            throw new RuntimeException("Limite atingido! A sala " + sala.getEndereco() +
                    " permite apenas " + sala.getLot_max() + " estações ocupadas simultaneamente.");
        }
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Vínculo não encontrado.");
        }
        repository.deleteById(id);
    }

    public List<EstacaoXReservaDTO> buscarTodos() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EstacaoXReservaDTO> buscarPorEstacao(Long idestacao) {
        return repository.findByIdestacao(idestacao).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<EstacaoXReservaDTO> buscarPorReserva(Long idreserva) {
        return repository.findByIdreserva(idreserva).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EstacaoXReservaDTO mapToDTO(EntEstacaoXReserva entidade) {
        return EstacaoXReservaDTO.builder()
                .id(entidade.getId())
                .idestacao(entidade.getIdestacao())
                .idreserva(entidade.getIdreserva())
                .build();
    }
}