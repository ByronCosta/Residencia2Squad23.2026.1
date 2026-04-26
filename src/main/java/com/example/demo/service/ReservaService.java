package com.example.demo.service;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.model.EntEstacaoXReserva;
import com.example.demo.model.EntReserva;
import com.example.demo.model.EntEstacao;
import com.example.demo.repository.EstacaoXReservaRepository;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.SalaRepository;
import com.example.demo.repository.EstacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private EstacaoXReservaRepository estacaoXReservaRepository;

    @Autowired
    private EstacaoRepository estacaoRepository;

    // --- ALGORITMOS DE BUSCA DE ESTAÇÕES ---

    private List<EntEstacao> buscarEstacoesJuntas(EntEstacao ref, List<EntEstacao> disponiveis, int total) {
        return disponiveis.stream()
                .sorted(Comparator.comparingDouble(e ->
                        Math.sqrt(Math.pow(e.getCoordx() - ref.getCoordx(), 2) +
                                Math.pow(e.getCoordy() - ref.getCoordy(), 2))))
                .limit(total)
                .collect(Collectors.toList());
    }

    private List<EntEstacao> buscarEstacoesSeparadas(List<EntEstacao> disponiveis, int salto, int total) {
        List<EntEstacao> resultado = new ArrayList<>();
        disponiveis.sort(Comparator.comparing(EntEstacao::getIdestacao));

        for (int i = 0; i < disponiveis.size() && resultado.size() < total; i += salto) {
            resultado.add(disponiveis.get(i));
        }
        return resultado;
    }

    // --- MÉTODOS PARA ADICIONAR RESERVAS COM LÓGICA (ALGORITMOS) ---

    public List<ReservaDTO> adicionarReservaEmGrupo(ReservaDTO baseDTO, int totalPessoas, Long idEstacaoReferencia) {
        EntEstacao ref = estacaoRepository.findById(idEstacaoReferencia)
                .orElseThrow(() -> new RuntimeException("Estação de referência não encontrada"));

        List<EntEstacao> disponiveis = estacaoRepository.buscarDisponiveisPorSala(baseDTO.getIdsala());
        List<EntEstacao> selecionadas = buscarEstacoesJuntas(ref, disponiveis, totalPessoas);

        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    public List<ReservaDTO> adicionarReservaSeparada(ReservaDTO baseDTO, int totalPessoas, int salto) {
        List<EntEstacao> disponiveis = estacaoRepository.buscarDisponiveisPorSala(baseDTO.getIdsala());
        List<EntEstacao> selecionadas = buscarEstacoesSeparadas(disponiveis, salto, totalPessoas);

        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    // --- MÉTODOS DE FILTRO E BUSCA ---

    public ReservaDTO buscarReservaPorId(Long id) {
        EntReserva reservab = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        return mapToDTO(reservab);
    }

    public List<ReservaDTO> buscarPorSala(Long idsala) {
        return reservaRepository.findByIdsala(idsala).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> buscarPorUsuario(Long idusuario) {
        return reservaRepository.findByIdusuario(idusuario).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> buscarPorProfissional(Long idprofissional) {
        return reservaRepository.findByIdprofissional(idprofissional).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS CRUD ORIGINAIS ---

    public ReservaDTO adicionarReserva(ReservaDTO reservaDTO) {
        validarDisponibilidade(reservaDTO);
        EntReserva entReserva = EntReserva.builder()
                .idsala(reservaDTO.getIdsala())
                .idusuario(reservaDTO.getIdusuario())
                .idprofissional(reservaDTO.getIdprofissional())
                .datainicial(reservaDTO.getDatainicial())
                .datafinal(reservaDTO.getDatafinal())
                .horainicial(reservaDTO.getHorainicial())
                .horafinal(reservaDTO.getHorafinal())
                .build();

        return mapToDTO(reservaRepository.save(entReserva));
    }

    private void validarDisponibilidade(ReservaDTO dto) {
        int lotMax = (int) salaRepository.findById(dto.getIdsala())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"))
                .getLot_max();

        Long totalOcupado = reservaRepository.somarOcupacaoNoPeriodo(
                dto.getIdsala(),
                dto.getDatainicial(),
                dto.getDatafinal(),
                dto.getHorainicial(),
                dto.getHorafinal()
        );

        if (totalOcupado != null && totalOcupado >= lotMax) {
            throw new RuntimeException("Capacidade máxima da sala atingida.");
        }
    }

    public ReservaDTO editarReserva(ReservaDTO reservaDTO) {
        EntReserva existente = reservaRepository.findById(reservaDTO.getIdreserva())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        validarDisponibilidade(reservaDTO);

        existente.setIdsala(reservaDTO.getIdsala());
        existente.setIdusuario(reservaDTO.getIdusuario());
        existente.setIdprofissional(reservaDTO.getIdprofissional());
        existente.setDatainicial(reservaDTO.getDatainicial());
        existente.setDatafinal(reservaDTO.getDatafinal());
        existente.setHorainicial(reservaDTO.getHorainicial());
        existente.setHorafinal(reservaDTO.getHorafinal());

        return mapToDTO(reservaRepository.save(existente));
    }

    public ReservaDTO deletarReserva(Long idreserva) {
        EntReserva existente = reservaRepository.findById(idreserva)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        reservaRepository.deleteById(idreserva);
        return mapToDTO(existente);
    }

    public List<ReservaDTO> buscarTodasReservas() {
        return reservaRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private List<ReservaDTO> salvarMultiplasReservas(ReservaDTO dto, List<EntEstacao> estacoes) {
        // 1. Cria a Reserva única (O "Pai")
        EntReserva reservaPai = EntReserva.builder()
                .idsala(dto.getIdsala())
                .idusuario(dto.getIdusuario())
                .idprofissional(dto.getIdprofissional())
                .datainicial(dto.getDatainicial())
                .datafinal(dto.getDatafinal())
                .horainicial(dto.getHorainicial())
                .horafinal(dto.getHorafinal())
                .build();

        EntReserva reservaSalva = reservaRepository.save(reservaPai);

        // 2. Cria os vínculos para cada estação selecionada pelo algoritmo
        for (EntEstacao estacao : estacoes) {
            EntEstacaoXReserva vinculo = EntEstacaoXReserva.builder()
                    .idreserva(reservaSalva.getIdreserva()) // ID da reserva única
                    .idestacao(estacao.getIdestacao())     // ID de cada estação do grupo
                    .build();
            estacaoXReservaRepository.save(vinculo);
        }

        // Retorna a reserva pai como DTO dentro de uma lista
        return List.of(mapToDTO(reservaSalva));
    }
    private ReservaDTO mapToDTO(EntReserva reserva) {
        return ReservaDTO.builder()
                .idreserva(reserva.getIdreserva())
                .idsala(reserva.getIdsala())
                .idusuario(reserva.getIdusuario())
                .idprofissional(reserva.getIdprofissional())
                .datainicial(reserva.getDatainicial())
                .datafinal(reserva.getDatafinal())
                .horainicial(reserva.getHorainicial())
                .horafinal(reserva.getHorafinal())
                .build();
    }
}