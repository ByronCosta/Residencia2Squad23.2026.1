package com.example.demo.service;

import com.example.demo.dto.EstacaoXReservaDTO;
import com.example.demo.model.*;
import com.example.demo.repository.EstacaoXReservaRepository;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.SalaRepository;
import com.example.demo.repository.UserRepository; // Importante
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

    @Autowired
    private UserRepository userRepository; // Injetado para validar a Role

    @Transactional
    public EstacaoXReservaDTO inserir(EstacaoXReservaDTO dto) {
        // 1. Recupera os dados da Reserva pai
        EntReserva reserva = reservaRepository.findById(dto.getIdreserva())
                .orElseThrow(() -> new RuntimeException("Reserva pai não encontrada."));

        // 2. Validações de Regra de Negócio (Capacidade e Perfil)
        validarCapacidadeDisponivel(reserva);
        validarRestricoesPerfilUser(reserva);

        EntEstacaoXReserva entidade = EntEstacaoXReserva.builder()
                .idestacao(dto.getIdestacao())
                .idreserva(dto.getIdreserva())
                .build();

        EntEstacaoXReserva salva = repository.save(entidade);
        return mapToDTO(salva);
    }

    /**
     * Aplica as travas para a Role USER e vínculo com Profissional
     */
    private void validarRestricoesPerfilUser(EntReserva reserva) {
        // Buscamos o usuário dono da reserva
        User usuario = userRepository.findById(reserva.getIdusuario())
                .orElseThrow(() -> new RuntimeException("Usuário dono da reserva não encontrado."));

        // Se for Role USER, aplicamos as restrições
        if ("USER".equals(usuario.getRole().name())) {

            // REGRA 1: Somente uma estação por reserva na tabela EstacaoXReserva
            long estacoesNestaReserva = repository.findByIdreserva(reserva.getIdreserva()).size();
            if (estacoesNestaReserva >= 1) {
                throw new RuntimeException("Usuários com perfil 'USER' só podem reservar uma estação (cadeira) por reserva.");
            }

            // REGRA 2: Se tiver idprofissional, só pode ter UMA reserva no sistema todo
            if (reserva.getIdprofissional() != null) {
                List<EntReserva> todasAsReservasDoUser = reservaRepository.findByIdusuario(reserva.getIdusuario());

                // Se ele já tem mais de uma reserva criada ou se está tentando adicionar estação a uma
                // reserva sendo que ele já possui histórico, bloqueamos.
                if (todasAsReservasDoUser.size() > 1) {
                    throw new RuntimeException("Usuário 'USER' com vínculo profissional só pode ter uma reserva ativa no sistema.");
                }
            }
        }
    }

    @Transactional
    public EstacaoXReservaDTO editar(Long id, EstacaoXReservaDTO dto) {
        EntEstacaoXReserva existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vínculo de reserva não encontrado"));

        if (!existente.getIdreserva().equals(dto.getIdreserva())) {
            EntReserva novaReserva = reservaRepository.findById(dto.getIdreserva())
                    .orElseThrow(() -> new RuntimeException("Nova reserva não encontrada."));

            validarCapacidadeDisponivel(novaReserva);
            validarRestricoesPerfilUser(novaReserva);
        }

        existente.setIdestacao(dto.getIdestacao());
        existente.setIdreserva(dto.getIdreserva());

        return mapToDTO(repository.save(existente));
    }

    // Método para buscar os vínculos por ID de Estação
    public List<EstacaoXReservaDTO> buscarPorEstacao(Long idestacao) {
        return repository.findByIdestacao(idestacao).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Método para buscar os vínculos por ID de Reserva
    public List<EstacaoXReservaDTO> buscarPorReserva(Long idreserva) {
        return repository.findByIdreserva(idreserva).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    private void validarCapacidadeDisponivel(EntReserva reserva) {
        EntSala sala = salaRepository.findById(reserva.getIdsala())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada."));

        Long ocupacaoAtual = repository.contarEstacoesOcupadasNoPeriodo(
                reserva.getDatainicial(),
                reserva.getDatafinal(),
                reserva.getHorainicial(),
                reserva.getHorafinal()
        );

        if (ocupacaoAtual >= sala.getLot_max()) {
            throw new RuntimeException("Limite atingido! A sala " + sala.getEndereco() +
                    " permite apenas " + sala.getLot_max() + " estações ocupadas.");
        }
    }

    // --- Demais métodos mantidos (deletar, buscarTodos, etc) ---

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Vínculo não encontrado.");
        }
        repository.deleteById(id);
    }

    public List<EstacaoXReservaDTO> buscarTodos() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private EstacaoXReservaDTO mapToDTO(EntEstacaoXReserva entidade) {
        return EstacaoXReservaDTO.builder()
                .id(entidade.getId())
                .idestacao(entidade.getIdestacao())
                .idreserva(entidade.getIdreserva())
                .build();
    }
}