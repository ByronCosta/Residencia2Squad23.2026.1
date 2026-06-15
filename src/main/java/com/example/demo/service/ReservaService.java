package com.example.demo.service;

import com.example.demo.dto.ReservaDTO;
import com.example.demo.dto.ReservaRequestDTO;
import com.example.demo.dto.SalaComEstacoesDTO;
import com.example.demo.dto.SalasEEstacoesDisponiveisDTO;
import com.example.demo.model.EntEstacaoXReserva;
import com.example.demo.model.EntReserva;
import com.example.demo.model.EntEstacao;
import com.example.demo.model.EntSala;
import com.example.demo.repository.EstacaoXReservaRepository;
import com.example.demo.repository.ReservaRepository;
import com.example.demo.repository.SalaRepository;
import com.example.demo.repository.EstacaoRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        if (total <= 0) return Collections.emptyList();
        return disponiveis.stream()
                .sorted(Comparator.comparingDouble(e ->
                        Math.sqrt(Math.pow(e.getCoordx() - ref.getCoordx(), 2) +
                                Math.pow(e.getCoordy() - ref.getCoordy(), 2))))
                .limit(total)
                .collect(Collectors.toList());
    }

    private List<EntEstacao> buscarEstacoesSeparadas(List<EntEstacao> disponiveis, int salto, int total) {
        if (total <= 0) return Collections.emptyList();
        List<EntEstacao> resultado = new ArrayList<>();
        disponiveis.sort(Comparator.comparing(EntEstacao::getIdestacao));

        for (int i = 0; i < disponiveis.size() && resultado.size() < total; i += salto) {
            resultado.add(disponiveis.get(i));
        }
        return resultado;
    }

    // --- MÉTODOS: POR PERFIL MULTI-CRITÉRIO (JUNTOS E SEPARADOS) ---

    /**
     * ALGORITMO 1: Busca estações livres por perfil (Dev/Design/Simples) no sistema todo.
     * Define dinamicamente uma âncora a partir do primeiro perfil disponível e agrupa o time.
     */
    @Transactional
    public List<ReservaDTO> adicionarReservaPorPerfilJuntos(ReservaRequestDTO perfilDTO) {
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> simplesLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "simples", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        // Validação estendida para os 3 perfis
        if (devsLivres.size() < perfilDTO.getQtdDev().intValue() ||
                designsLivres.size() < perfilDTO.getQtdDesign().intValue() ||
                simplesLivres.size() < perfilDTO.getQtdSimples().intValue()) {
            throw new RuntimeException("Não há estações livres suficientes no sistema para a quantidade de perfis solicitada.");
        }

        // Define a âncora prioritariamente por Dev, senão Design, senão Simples
        EntEstacao ref = !devsLivres.isEmpty() ? devsLivres.get(0) :
                (!designsLivres.isEmpty() ? designsLivres.get(0) : simplesLivres.get(0));

        Long idSalaDinamico = ref.getIdsala();

        List<EntEstacao> selecionadas = new ArrayList<>();
        selecionadas.addAll(buscarEstacoesJuntas(ref, devsLivres, perfilDTO.getQtdDev().intValue()));
        selecionadas.addAll(buscarEstacoesJuntas(ref, designsLivres, perfilDTO.getQtdDesign().intValue()));
        selecionadas.addAll(buscarEstacoesJuntas(ref, simplesLivres, perfilDTO.getQtdSimples().intValue()));

        ReservaDTO baseDTO = converterParaReservaDTO(perfilDTO, idSalaDinamico);
        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    /**
     * ALGORITMO 2: Busca estações livres por perfil (Dev/Design/Simples) no sistema todo e aplica o salto.
     */
    @Transactional
    public List<ReservaDTO> adicionarReservaPorPerfilSeparados(ReservaRequestDTO perfilDTO, int salto) {
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> simplesLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "simples", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        if (devsLivres.size() < perfilDTO.getQtdDev().intValue() ||
                designsLivres.size() < perfilDTO.getQtdDesign().intValue() ||
                simplesLivres.size() < perfilDTO.getQtdSimples().intValue()) {
            throw new RuntimeException("Não há estações livres suficientes no sistema para a quantidade de perfis solicitada.");
        }

        // Descobre a sala baseada no primeiro elemento encontrado da lista geral
        EntEstacao ref = !devsLivres.isEmpty() ? devsLivres.get(0) :
                (!designsLivres.isEmpty() ? designsLivres.get(0) : simplesLivres.get(0));
        Long idSalaDinamico = ref.getIdsala();

        List<EntEstacao> selecionadas = new ArrayList<>();
        selecionadas.addAll(buscarEstacoesSeparadas(devsLivres, salto, perfilDTO.getQtdDev().intValue()));
        selecionadas.addAll(buscarEstacoesSeparadas(designsLivres, salto, perfilDTO.getQtdDesign().intValue()));
        selecionadas.addAll(buscarEstacoesSeparadas(simplesLivres, salto, perfilDTO.getQtdSimples().intValue()));

        ReservaDTO baseDTO = converterParaReservaDTO(perfilDTO, idSalaDinamico);
        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    // --- CONSULTAS MULTI-PERFIL ---
    /**
     * CONSULTA: Retorna as salas com capacidade conjunta E as respectivas estações selecionadas.
     */
    @Transactional(readOnly = true)
    public List<SalaComEstacoesDTO> consultarSalasDisponiveisJuntos(ReservaRequestDTO perfilDTO) {
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> simplesLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "simples", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        if (devsLivres.size() < perfilDTO.getQtdDev().intValue() ||
                designsLivres.size() < perfilDTO.getQtdDesign().intValue() ||
                simplesLivres.size() < perfilDTO.getQtdSimples().intValue()) {
            return Collections.emptyList();
        }

        Set<Long> idsSalasValidas = new HashSet<>();
        List<SalaComEstacoesDTO> salasDisponiveis = new ArrayList<>();

        List<EntEstacao> todasAsLivres = new ArrayList<>();
        todasAsLivres.addAll(devsLivres);
        todasAsLivres.addAll(designsLivres);
        todasAsLivres.addAll(simplesLivres);

        for (EntEstacao ref : todasAsLivres) {
            Long idSalaAtual = ref.getIdsala();

            if (idsSalasValidas.contains(idSalaAtual)) continue;

            List<EntEstacao> devsDaSala = devsLivres.stream().filter(e -> e.getIdsala().equals(idSalaAtual)).collect(Collectors.toList());
            List<EntEstacao> designsDaSala = designsLivres.stream().filter(e -> e.getIdsala().equals(idSalaAtual)).collect(Collectors.toList());
            List<EntEstacao> simplesDaSala = simplesLivres.stream().filter(e -> e.getIdsala().equals(idSalaAtual)).collect(Collectors.toList());

            if (devsDaSala.size() >= perfilDTO.getQtdDev().intValue() &&
                    designsDaSala.size() >= perfilDTO.getQtdDesign().intValue() &&
                    simplesDaSala.size() >= perfilDTO.getQtdSimples().intValue()) {
                try {
                    // Captura as sublistas exatas validadas pelo algoritmo geométrico/proximidade
                    List<EntEstacao> devsEscolhidos = buscarEstacoesJuntas(ref, devsDaSala, perfilDTO.getQtdDev().intValue());
                    List<EntEstacao> designsEscolhidos = buscarEstacoesJuntas(ref, designsDaSala, perfilDTO.getQtdDesign().intValue());
                    List<EntEstacao> simplesEscolhidos = buscarEstacoesJuntas(ref, simplesDaSala, perfilDTO.getQtdSimples().intValue());

                    // Une todas as estações validadas desta sala específica
                    List<EntEstacao> todasEstacoesDaSala = new ArrayList<>();
                    todasEstacoesDaSala.addAll(devsEscolhidos);
                    todasEstacoesDaSala.addAll(designsEscolhidos);
                    todasEstacoesDaSala.addAll(simplesEscolhidos);

                    idsSalasValidas.add(idSalaAtual);

                    // Busca a entidade Sala e monta o DTO com suas estações
                    salaRepository.findById(idSalaAtual).ifPresent(sala -> {
                        salasDisponiveis.add(new SalaComEstacoesDTO(sala, todasEstacoesDaSala));
                    });

                } catch (Exception e) {
                    // Geometria inválida para essa âncora, passa para o próximo loop
                }
            }
        }
        return salasDisponiveis;
    }

    /**
     * CONSULTA: Retorna todas as salas e suas respectivas estações que estão 100% livres no período.
     */
    @Transactional(readOnly = true)
    public List<SalasEEstacoesDisponiveisDTO> consultarTodasSalasEEstacoesLivres(ReservaRequestDTO perfilDTO) {
        // 1. Busca todas as estações de todos os perfis que estão livres no período
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> simplesLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "simples", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        // 2. Une todas as estações encontradas em uma única lista global
        List<EntEstacao> todasAsLivres = new ArrayList<>();
        todasAsLivres.addAll(devsLivres);
        todasAsLivres.addAll(designsLivres);
        todasAsLivres.addAll(simplesLivres);

        // 3. Agrupa as estações pelo ID da Sala usando Stream Map (Chave: IdSala, Valor: Lista de Estações)
        Map<Long, List<EntEstacao>> estacoesAgrupadasPorSala = todasAsLivres.stream()
                .collect(Collectors.groupingBy(EntEstacao::getIdsala));

        List<SalasEEstacoesDisponiveisDTO> resultado = new ArrayList<>();

        // 4. Para cada grupo mapeado, busca a Sala no banco e monta o DTO de retorno
        for (Map.Entry<Long, List<EntEstacao>> entry : estacoesAgrupadasPorSala.entrySet()) {
            Long idSala = entry.getKey();
            List<EntEstacao> estacoesDaSala = entry.getValue();

            salaRepository.findById(idSala).ifPresent(sala -> {
                resultado.add(new SalasEEstacoesDisponiveisDTO(sala, estacoesDaSala));
            });
        }

        return resultado;
    }
    /**
     * CONSULTA: Retorna as salas com capacidade para os 3 perfis aplicando regras de salto.
     */
    @Transactional(readOnly = true)
    public List<EntSala> consultarSalasDisponiveisSeparados(ReservaRequestDTO perfilDTO, int salto) {
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> simplesLivres = estacaoRepository.buscarEstacoesLivresPorPerfilSemSala(
                "simples", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        if (devsLivres.size() < perfilDTO.getQtdDev().intValue() ||
                designsLivres.size() < perfilDTO.getQtdDesign().intValue() ||
                simplesLivres.size() < perfilDTO.getQtdSimples().intValue()) {
            return Collections.emptyList();
        }

        Set<Long> idsSalasValidas = new HashSet<>();
        List<EntSala> salasDisponiveis = new ArrayList<>();

        Map<Long, List<EntEstacao>> devsPorSala = devsLivres.stream().collect(Collectors.groupingBy(EntEstacao::getIdsala));
        Map<Long, List<EntEstacao>> designsPorSala = designsLivres.stream().collect(Collectors.groupingBy(EntEstacao::getIdsala));
        Map<Long, List<EntEstacao>> simplesPorSala = simplesLivres.stream().collect(Collectors.groupingBy(EntEstacao::getIdsala));

        // Avalia todas as chaves de salas que possuem devs (ou use outra lista global de IDs de salas mapeadas)
        Set<Long> todasAsSalasId = new HashSet<>(devsPorSala.keySet());
        todasAsSalasId.addAll(designsPorSala.keySet());
        todasAsSalasId.addAll(simplesPorSala.keySet());

        for (Long idSala : todasAsSalasId) {
            List<EntEstacao> devsDaSala = devsPorSala.getOrDefault(idSala, Collections.emptyList());
            List<EntEstacao> designsDaSala = designsPorSala.getOrDefault(idSala, Collections.emptyList());
            List<EntEstacao> simplesDaSala = simplesPorSala.getOrDefault(idSala, Collections.emptyList());

            if (devsDaSala.size() >= perfilDTO.getQtdDev().intValue() &&
                    designsDaSala.size() >= perfilDTO.getQtdDesign().intValue() &&
                    simplesDaSala.size() >= perfilDTO.getQtdSimples().intValue()) {
                try {
                    buscarEstacoesSeparadas(devsDaSala, salto, perfilDTO.getQtdDev().intValue());
                    buscarEstacoesSeparadas(designsDaSala, salto, perfilDTO.getQtdDesign().intValue());
                    buscarEstacoesSeparadas(simplesDaSala, salto, perfilDTO.getQtdSimples().intValue());

                    idsSalasValidas.add(idSala);
                    salaRepository.findById(idSala).ifPresent(salasDisponiveis::add);
                } catch (Exception e) {
                    // Salto inviável para esta combinação de espaço
                }
            }
        }
        return salasDisponiveis;
    }

    // --- ALGORITMOS ALOCADOS POR SALA ESPECÍFICA ---

    @Transactional
    public List<ReservaDTO> adicionarReservaPorPerfilJuntosNaSala(ReservaRequestDTO perfilDTO, Long idSala) {
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilESala(
                idSala, "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilESala(
                idSala, "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> simplesLivres = estacaoRepository.buscarEstacoesLivresPorPerfilESala(
                idSala, "simples", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        if (devsLivres.size() < perfilDTO.getQtdDev().intValue() ||
                designsLivres.size() < perfilDTO.getQtdDesign().intValue() ||
                simplesLivres.size() < perfilDTO.getQtdSimples().intValue()) {
            throw new RuntimeException("Não há estações livres suficientes nesta sala para os perfis solicitados.");
        }

        // Escolha da âncora local à sala
        EntEstacao ref = !devsLivres.isEmpty() ? devsLivres.get(0) :
                (!designsLivres.isEmpty() ? designsLivres.get(0) : simplesLivres.get(0));

        List<EntEstacao> selecionadas = new ArrayList<>();
        selecionadas.addAll(buscarEstacoesJuntas(ref, devsLivres, perfilDTO.getQtdDev().intValue()));
        selecionadas.addAll(buscarEstacoesJuntas(ref, designsLivres, perfilDTO.getQtdDesign().intValue()));
        selecionadas.addAll(buscarEstacoesJuntas(ref, simplesLivres, perfilDTO.getQtdSimples().intValue()));

        ReservaDTO baseDTO = converterParaReservaDTO(perfilDTO, idSala);
        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    @Transactional
    public List<ReservaDTO> adicionarReservaPorPerfilSeparadosNaSala(ReservaRequestDTO perfilDTO, int salto, Long idSala) {
        List<EntEstacao> devsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilESala(
                idSala, "dev", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> designsLivres = estacaoRepository.buscarEstacoesLivresPorPerfilESala(
                idSala, "design", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        List<EntEstacao> simplesLivres = estacaoRepository.buscarEstacoesLivresPorPerfilESala(
                idSala, "simples", perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

        if (devsLivres.size() < perfilDTO.getQtdDev().intValue() ||
                designsLivres.size() < perfilDTO.getQtdDesign().intValue() ||
                simplesLivres.size() < perfilDTO.getQtdSimples().intValue()) {
            throw new RuntimeException("Não há estações livres suficientes nesta sala para os perfis solicitados.");
        }

        List<EntEstacao> selecionadas = new ArrayList<>();
        selecionadas.addAll(buscarEstacoesSeparadas(devsLivres, salto, perfilDTO.getQtdDev().intValue()));
        selecionadas.addAll(buscarEstacoesSeparadas(designsLivres, salto, perfilDTO.getQtdDesign().intValue()));
        selecionadas.addAll(buscarEstacoesSeparadas(simplesLivres, salto, perfilDTO.getQtdSimples().intValue()));

        ReservaDTO baseDTO = converterParaReservaDTO(perfilDTO, idSala);
        return salvarMultiplasReservas(baseDTO, selecionadas);
    }

    // --- MÉTODOS AUXILIARES E ORIGINAIS CONSERVADOS ---

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
        return reservaRepository.findByIdusuario(idusuario).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ReservaDTO> buscarPorProfissional(Long idprofissional) {
        return reservaRepository.findByIdprofissional(idprofissional).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // --- CORREÇÃO DA IMAGEM 2 (adicionarReserva) ---
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

        // Salva a entidade e mapeia o resultado de volta para um único DTO
        EntReserva salva = reservaRepository.save(entReserva);
        return mapToDTO(salva);
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