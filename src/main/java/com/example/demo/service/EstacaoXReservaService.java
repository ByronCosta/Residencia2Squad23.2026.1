package com.example.demo.service;

import com.example.demo.dto.EstacaoXReservaDTO;
import com.example.demo.dto.ReservaRequestDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
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
    private UserRepository userRepository;

    @Autowired
    private EstacaoRepository estacaoRepository;

    /**
     * NOVO MÉTODO: Realiza reserva automática buscando salas com capacidade de perfis (Dev/Design)
     */
    /*@Transactional
    public EntReserva realizarReservaPorPerfis(ReservaRequestDTO dto) {
        // 1. Busca as salas que têm a capacidade física dos perfis
        List<EntSala> salasEmPotencial = salaRepository.buscarSalasPorCapacidadeDePerfis(dto.getQtdDev(), dto.getQtdDesign());

        if (salasEmPotencial.isEmpty()) {
            throw new RuntimeException("Não há salas com essa capacidade de perfis cadastrada.");
        }

        // 2. Varre as salas para encontrar uma que tenha as estações LIVRES no horário
        for (EntSala sala : salasEmPotencial) {
            List<EntEstacao> estacoesDevLivres = estacaoRepository.buscarEstacoesLivresPorPerfil(sala.getIdsala(), "dev", dto.getDataInicio().toLocalDate(), dto.getDataFim().toLocalDate());
            List<EntEstacao> estacoesDesignLivres = estacaoRepository.buscarEstacoesLivresPorPerfil(sala.getIdsala(), "design", dto.getDataInicio().toLocalDate(), dto.getDataFim().toLocalDate());

            // Se a sala tiver assentos livres suficientes para ambos os perfis no horário:
            if (estacoesDevLivres.size() >= dto.getQtdDev() && estacoesDesignLivres.size() >= dto.getQtdDesign()) {

                EntReserva novaReserva = new EntReserva();
                novaReserva.setDatainicial(dto.getDataInicio().toLocalDate()); // Ajustado para bater com seu modelo EntReserva
                novaReserva.setDatafinal(dto.getDataFim().toLocalDate());
                novaReserva.setIdsala(sala.getIdsala());
                // novaReserva.setIdusuario(dto.getIdUsuario()); // Vincule o usuário aqui se vier no DTO

                novaReserva = reservaRepository.save(novaReserva);

                // 3. Salva os vínculos na tabela intermediária limitando à quantidade pedida
                vincularAssentosAReserva(novaReserva, estacoesDevLivres, dto.getQtdDev().intValue());
                vincularAssentosAReserva(novaReserva, estacoesDesignLivres, dto.getQtdDesign().intValue());

                return novaReserva;
            }
        }

        throw new RuntimeException("Nenhuma sala possui essa quantidade de estações LIVRES no horário selecionado.");
    }
*/
    private void vincularAssentosAReserva(EntReserva reserva, List<EntEstacao> estacoesLivres, Integer quantidadeNecessaria) {
        for (int i = 0; i < quantidadeNecessaria; i++) {
            EntEstacao estacao = estacoesLivres.get(i);

            EntEstacaoXReserva vinculo = EntEstacaoXReserva.builder()
                    .idreserva(reserva.getIdreserva())
                    .idestacao(estacao.getIdestacao())
                    .build();

            repository.save(vinculo);
        }
    }

    /**
     * MÉTODO EXISTENTE: Inserção manual de uma única estação em uma reserva
     */
    @Transactional
    public EstacaoXReservaDTO inserir(EstacaoXReservaDTO dto) {
        EntReserva reserva = reservaRepository.findById(dto.getIdreserva())
                .orElseThrow(() -> new RuntimeException("Reserva pai não encontrada."));

        validarCapacidadeDisponivel(reserva);
        validarRestricoesPerfilUser(reserva);

        EntEstacaoXReserva entidade = EntEstacaoXReserva.builder()
                .idestacao(dto.getIdestacao())
                .idreserva(dto.getIdreserva())
                .build();

        EntEstacaoXReserva salva = repository.save(entidade);
        return mapToDTO(salva);
    }

    private void validarRestricoesPerfilUser(EntReserva reserva) {
        User usuario = userRepository.findById(reserva.getIdusuario())
                .orElseThrow(() -> new RuntimeException("Usuário dono da reserva não encontrado."));

        if ("USER".equals(usuario.getRole().name())) {
            long estacoesNestaReserva = repository.findByIdreserva(reserva.getIdreserva()).size();
            if (estacoesNestaReserva >= 1) {
                throw new RuntimeException("Usuários com perfil 'USER' só podem reservar uma estação por reserva.");
            }

            if (reserva.getIdprofissional() != null) {
                List<EntReserva> todasAsReservasDoUser = reservaRepository.findByIdusuario(reserva.getIdusuario());
                if (todasAsReservasDoUser.size() > 1) {
                    throw new RuntimeException("Usuário 'USER' com vínculo profissional só pode ter uma reserva ativa.");
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

    public List<EstacaoXReservaDTO> buscarPorEstacao(Long idestacao) {
        return repository.findByIdestacao(idestacao).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<EstacaoXReservaDTO> buscarPorReserva(Long idreserva) {
        return repository.findByIdreserva(idreserva).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) throw new RuntimeException("Vínculo não encontrado.");
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