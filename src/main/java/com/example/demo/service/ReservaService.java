package com.example.demo.service;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.model.EntReserva;
import com.example.demo.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public ReservaDTO adicionarReserva(ReservaDTO reservaDTO) {
        EntReserva entReserva = EntReserva.builder()
                .idsala(reservaDTO.getIdsala())
                .idusuario(reservaDTO.getIdusuario())
                .idprofissional(reservaDTO.getIdprofissional())
                .datainicial(reservaDTO.getDatainicial())
                .datafinal(reservaDTO.getDatafinal())
                .horainicial(reservaDTO.getHorainicial()) // Faltava este campo no builder
                .tempo(reservaDTO.getTempo())
                .build();

        // Salva no banco e captura a entidade com o ID gerado
        EntReserva entidadeSalva = reservaRepository.save(entReserva);

        // Retorna o DTO convertido da entidade salva (agora com ID!)
        return mapToDTO(entidadeSalva);
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

        // Atualiza os campos
        reservaexistente.setIdsala(reservaDTO.getIdsala());
        reservaexistente.setIdusuario(reservaDTO.getIdusuario());
        reservaexistente.setIdprofissional(reservaDTO.getIdprofissional());
        reservaexistente.setDatainicial(reservaDTO.getDatainicial());
        reservaexistente.setDatafinal(reservaDTO.getDatafinal());
        reservaexistente.setHorainicial(reservaDTO.getHorainicial());
        reservaexistente.setTempo(reservaDTO.getTempo());

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

    public List<ReservaDTO> buscarReservaComplexa(LocalDate dataInicio, LocalDate dataFim, String hora, String tempo) {
        java.sql.Time horaConvertida = java.sql.Time.valueOf(hora);
        Double tempoConvertido = Double.valueOf(tempo);

        return reservaRepository.findByDatainicialAndDatafinalAndHorainicialAndTempo(dataInicio, dataFim, horaConvertida, tempoConvertido)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Método centralizado para conversão
    private ReservaDTO mapToDTO(EntReserva reserva) {
        return ReservaDTO.builder()
                .idreserva(reserva.getIdreserva())
                .idsala(reserva.getIdsala())
                .idusuario(reserva.getIdusuario())
                .idprofissional(reserva.getIdprofissional())
                .datainicial(reserva.getDatainicial())
                .datafinal(reserva.getDatafinal())
                .horainicial(reserva.getHorainicial())
                .tempo(reserva.getTempo())
                .build();
    }
}