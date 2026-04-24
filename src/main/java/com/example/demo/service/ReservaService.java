package com.example.demo.service;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.model.EntReserva;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SalaRepository salaRepository;

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

        EntReserva entidadeSalva = reservaRepository.save(entReserva);
        return mapToDTO(entidadeSalva);
    }
    private void validarDisponibilidade(ReservaDTO dto) {
        int lotMax = salaRepository.findById(dto.getIdsala())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"))
                .getLot_max();

        // 1. Mude para Long para bater com o retorno do Repository
        // 2. Use .toLocalTime() se os campos do DTO forem java.sql.Time
        Long totalOcupado = reservaRepository.somarOcupacaoNoPeriodo(
                dto.getIdsala(),
                dto.getDatainicial(),
                dto.getDatafinal(),
                dto.getHorainicial(),
                dto.getHorafinal()
        );

        if (totalOcupado >= lotMax) {
            throw new RuntimeException("Capacidade máxima da sala atingida.");
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

    // Busca todas as reservas de uma sala específica
    public List<ReservaDTO> buscarPorSala(Long idsala) {
        return reservaRepository.findByIdsala(idsala).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Busca todas as reservas de um usuário específico
    public List<ReservaDTO> buscarPorUsuario(Long idusuario) {
        return reservaRepository.findByIdusuario(idusuario).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Busca todas as reservas de um profissional específico
    public List<ReservaDTO> buscarPorProfissional(Long idprofissional) {
        return reservaRepository.findByIdprofissional(idprofissional).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Métodos de busca omitidos para brevidade, mas seguem a mesma lógica...

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