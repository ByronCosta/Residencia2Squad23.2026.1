package com.example.demo.service;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.model.EntReserva;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.SalaRepository; // Import necessário
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SalaRepository salaRepository; // Injetado para verificar lot_max

    public ReservaDTO adicionarReserva(ReservaDTO reservaDTO) {
        // 1. PRIMEIRO: Valida se há espaço (Regra de Negócio)
        // Se estourar a lotação, o código para aqui e lança a Exception
        validarDisponibilidade(reservaDTO);

        // 2. SEGUNDO: Se passou na validação, constrói a entidade
        EntReserva entReserva = EntReserva.builder()
                .idsala(reservaDTO.getIdsala())
                .idusuario(reservaDTO.getIdusuario())
                .idprofissional(reservaDTO.getIdprofissional())
                .datainicial(reservaDTO.getDatainicial())
                .datafinal(reservaDTO.getDatafinal())
                .horainicial(reservaDTO.getHorainicial())
                .horafinal(reservaDTO.getHorafinal())
                .build();

        // 3. TERCEIRO: Persiste no banco
        EntReserva entidadeSalva = reservaRepository.save(entReserva);

        return mapToDTO(entidadeSalva);
    }
    /**
     * Método de validação de Regra de Negócio:
     * Compara a soma das estações ocupadas com a lotação máxima da sala.
     */
    private void validarDisponibilidade(ReservaDTO dto) {
        // Busca a lotação máxima da sala
        int lotMax = salaRepository.findById(dto.getIdsala())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"))
                .getLot_max();

        // Busca quantas estações já estão reservadas no período (COUNT da EstacaoXReserva)
        Integer totalOcupado = reservaRepository.somarOcupacaoNoPeriodo(
                dto.getIdsala(),
                dto.getDatainicial(),
                dto.getDatafinal(),
                dto.getHorainicial(),
                dto.getHorafinal()
        );

        int ocupacaoAtual = (totalOcupado != null) ? totalOcupado : 0;

        // Verifica se adicionar mais 1 reserva excede o limite
        if (ocupacaoAtual + 1 > lotMax) {
            throw new RuntimeException("Capacidade máxima da sala atingida para este horário. " +
                    "Ocupação: " + ocupacaoAtual + "/" + lotMax);
        }
    }

    public ReservaDTO buscarReservaPorId(ReservaDTO reserva) {
        EntReserva reservab = reservaRepository.findById(reserva.getIdreserva())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        return mapToDTO(reservab);
    }

    public List<ReservaDTO> buscarTodasReservas() {
        return reservaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReservaDTO editarReserva(ReservaDTO reservaDTO) {
        EntReserva reservaexistente = reservaRepository.findById(reservaDTO.getIdreserva())
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada com o ID: " + reservaDTO.getIdreserva()));

        // Opcional: Se mudar data/hora na edição, deveria validar disponibilidade novamente
        validarDisponibilidade(reservaDTO);

        reservaexistente.setIdsala(reservaDTO.getIdsala());
        reservaexistente.setIdusuario(reservaDTO.getIdusuario());
        reservaexistente.setIdprofissional(reservaDTO.getIdprofissional());
        reservaexistente.setDatainicial(reservaDTO.getDatainicial());
        reservaexistente.setDatafinal(reservaDTO.getDatafinal());
        reservaexistente.setHorainicial(reservaDTO.getHorainicial());
        reservaexistente.setHorafinal(reservaDTO.getHorafinal());

        EntReserva salva = reservaRepository.save(reservaexistente);
        return mapToDTO(salva);
    }

    public ReservaDTO deletarReserva(Long idreserva) {
        EntReserva reservaexistente = reservaRepository.findById(idreserva)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        reservaRepository.deleteById(idreserva);
        return mapToDTO(reservaexistente);
    }

    public List<ReservaDTO> buscarPorSala(Long idsala) {
        return reservaRepository.findByIdsala(idsala).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReservaDTO> buscarEntreDatas(LocalDate inicio, LocalDate fim) {
        return reservaRepository.findByDatainicialAfterAndDatafinalBefore(inicio, fim).stream()
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

    public List<ReservaDTO> buscarPorDataInicial(LocalDate data) {
        return reservaRepository.findByDatainicial(data).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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