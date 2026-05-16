package com.example.demo.service;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.dto.ReservaRequestDTO;
import com.example.demo.model.EntEstacaoXReserva;
import com.example.demo.model.EntReserva;
import com.example.demo.model.EntEstacao;
import com.example.demo.repository.EstacaoXReservaRepository;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.SalaRepository;
import com.example.demo.repository.EstacaoRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private UserRepository userRepository;

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

    // --- MÉTODOS: POR PERFIL (JUNTOS E SEPARADOS) ---

    /**
     * ALGORITMO 1: Busca estações livres por perfil (Dev/Design) no sistema todo.
     * Define automaticamente a primeira estação de Dev livre como referência
     * e agrupa o restante do time por proximidade a partir dela.
     */
    @Transactional
    public List<ReservaDTO> adicionarReservaPorPerfilJuntos(ReservaRequestDTO perfilDTO) {
        // 1. Busca as estações LIVRES filtradas por perfil no banco (Sistema completo)
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        // 2. Valida se existem cadeiras livres suficientes no sistema todo
        if (devsLivres.isEmpty() || devsLivres.size() < perfilDTO.getQtdDev().intValue() || designsLivres.size() < perfilDTO.getQtdDesign().intValue()) {
            throw new RuntimeException("Não há estações livres suficientes no sistema para a quantidade de perfis solicitada.");
        }

        // 3. Define a primeira estação disponível como âncora do time
        EntEstacao ref = devsLivres.get(0);

        // 4. Descobre dinamicamente o ID da sala com base na estação âncora escolhida
        Long idSalaDinamico = ref.getIdsala();

        // 5. Aplica o cálculo de proximidade geométrica baseado na estação âncora
        List<EntEstacao> selecionadas = new ArrayList<>();
        selecionadas.addAll(buscarEstacoesJuntas(ref, devsLivres, perfilDTO.getQtdDev().intValue()));
        selecionadas.addAll(buscarEstacoesJuntas(ref, designsLivres, perfilDTO.getQtdDesign().intValue()));

        // 6. Converte para o DTO passando o ID da sala que o algoritmo escolheu
        ReservaDTO baseDTO = converterParaReservaDTO(perfilDTO, idSalaDinamico);

        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    /**
     * ALGORITMO 2: Busca estações livres por perfil (Dev/Design) no sistema todo e espalha o time
     * aplicando um salto de índices. Descobre a sala dinamicamente.
     */
    @Transactional
    public List<ReservaDTO> adicionarReservaPorPerfilSeparados(ReservaRequestDTO perfilDTO, int salto) {
        // 1. Busca as estações LIVRES filtradas por perfil no banco (Sistema completo)
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        // 2. Valida se há estações suficientes livres no sistema
        if (devsLivres.isEmpty() || devsLivres.size() < perfilDTO.getQtdDev().intValue() || designsLivres.size() < perfilDTO.getQtdDesign().intValue()) {
            throw new RuntimeException("Não há estações livres suficientes no sistema para a quantidade de perfis solicitada.");
        }

        // 3. Descobre dinamicamente o ID da sala com base na primeira estação de dev encontrada
        Long idSalaDinamico = devsLivres.get(0).getIdsala();

        // 4. Seleciona as estações aplicando a lógica do salto
        List<EntEstacao> selecionadas = new ArrayList<>();
        selecionadas.addAll(buscarEstacoesSeparadas(devsLivres, salto, perfilDTO.getQtdDev().intValue()));
        selecionadas.addAll(buscarEstacoesSeparadas(designsLivres, salto, perfilDTO.getQtdDesign().intValue()));

        // 5. Converte para o DTO passando o ID da sala obtido dinamicamente
        ReservaDTO baseDTO = converterParaReservaDTO(perfilDTO, idSalaDinamico);

        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    private ReservaDTO converterParaReservaDTO(ReservaRequestDTO perfilDTO, Long idSala) {
        return ReservaDTO.builder()
                .idsala(idSala)
                .idusuario(perfilDTO.getIdUsuario())
                .datainicial(perfilDTO.getDataInicio().toLocalDate())
                .datafinal(perfilDTO.getDataFim().toLocalDate())
                .horainicial(perfilDTO.getDataInicio().toLocalTime())
                .horafinal(perfilDTO.getDataFim().toLocalTime())
                .build();
    }

    // --- MÉTODOS PARA ADICIONAR RESERVAS COM LÓGICA (ALGORITMOS ORIGINAIS) ---

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
        validarRestricaoUsuarioComProfissional(reservaDTO);

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

    private void validarRestricaoUsuarioComProfissional(ReservaDTO dto) {
        if (dto.getIdusuario() == null && dto.getIdprofissional() == null) {
            throw new RuntimeException("A reserva deve estar vinculada a um Usuário ou a um Profissional.");
        }

        if (dto.getIdusuario() != null) {
            com.example.demo.model.User usuario = userRepository.findById(dto.getIdusuario())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

            if ("USER".equals(usuario.getRole().name())) {
                List<EntReserva> reservasAtivas = reservaRepository.findByIdusuario(dto.getIdusuario());
                if (!reservasAtivas.isEmpty()) {
                    throw new RuntimeException("Usuários comuns só podem possuir uma reserva ativa.");
                }
            }
        }

        if (dto.getIdprofissional() != null) {
            List<EntReserva> reservasProfissional = reservaRepository.findByIdprofissional(dto.getIdprofissional());
            if (!reservasProfissional.isEmpty()) {
                throw new RuntimeException("Este profissional já possui uma reserva ativa e não pode realizar outra.");
            }
        }
    }

    public void validarDisponibilidade(ReservaDTO dto) {
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

        for (EntEstacao estacao : estacoes) {
            EntEstacaoXReserva vinculo = EntEstacaoXReserva.builder()
                    .idreserva(reservaSalva.getIdreserva())
                    .idestacao(estacao.getIdestacao())
                    .build();
            estacaoXReservaRepository.save(vinculo);
        }

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