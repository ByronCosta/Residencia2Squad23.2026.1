package com.example.demo.controller;

import com.example.demo.dto.ReservaComEstacoesResponseDTO;
import com.example.demo.dto.ReservaDTO;
import com.example.demo.dto.ReservaRequestDTO;
import com.example.demo.dto.EstacaoCoordenadasDTO;
import com.example.demo.model.EntSala;
import com.example.demo.model.EntEstacaoXReserva; // Tipo corrigido aqui
import com.example.demo.service.ReservaService;
import com.example.demo.repository.EstacaoRepository;
import com.example.demo.repository.EstacaoXReservaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private EstacaoRepository estacaoRepository;

    @Autowired
    private EstacaoXReservaRepository estacaoXReservaRepository;

    // =========================================================================
    // --- ESTRATÉGIA 1: POR PERFIL (SEM SALA - SISTEMA DECIDE A SALA) ---
    // =========================================================================

    /**
     * ALGORITMO 1 (SEM SALA): Reserva assentos de 3 Perfis juntos (Proximidade Euclidiana)
     * O sistema varre todas as salas e aloca onde houver espaço conjunto.
     * POST http://localhost:8080/reservas/por-perfil/juntos
     */
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    @PostMapping("/por-perfil/juntos")
    public ResponseEntity<?> reservar3PerfisJuntosSemSala(
            @RequestBody ReservaRequestDTO perfilDTO) {
        try {
            List<ReservaDTO> reservas = reservaService.adicionarReservaPorPerfilJuntos(perfilDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ALGORITMO 2 (SEM SALA): Reserva assentos de 3 Perfis separados (Estratégia de Salto)
     * O sistema varre todas as salas aplicando o salto de cadeiras onde for viável.
     * POST http://localhost:8080/reservas/por-perfil/separados?salto=2
     */
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    @PostMapping("/por-perfil/separados")
    public ResponseEntity<?> reservar3PerfisSeparadosSemSala(
            @RequestParam(defaultValue = "1") int salto,
            @RequestBody ReservaRequestDTO perfilDTO) {
        try {
            List<ReservaDTO> reservas = reservaService.adicionarReservaPorPerfilSeparados(perfilDTO, salto);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================================================================
    // --- ESTRATÉGIA 2: POR PERFIL (COM SALA FIXA - USUÁRIO ESCOLHEU A SALA) ---
    // =========================================================================

    /**
     * ALGORITMO 1 (COM SALA): Reserva assentos de 3 Perfis juntos dentro de uma sala fixa
     * POST http://localhost:8080/reservas/por-perfil/juntos/sala/{idSala}
     */
    @PostMapping("/por-perfil/juntos/sala/{idSala}")
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    public ResponseEntity<ReservaComEstacoesResponseDTO> reservar3PerfisJuntosPorSala(
            @PathVariable Long idSala,
            @RequestBody ReservaRequestDTO perfilDTO) {

        // CORREÇÃO 1: Parâmetros invertidos para bater exatamente com a assinatura da sua Service
        List<ReservaDTO> reservasCriadas = reservaService.adicionarReservaPorPerfilJuntosNaSala(perfilDTO, idSala);

        List<EstacaoCoordenadasDTO> estacoesMapeadas = new ArrayList<>();

        // Busca os vínculos reais gerados no banco para montar o payload de resposta
        for (ReservaDTO reserva : reservasCriadas) {
            if (reserva.getIdreserva() != null) {

                List<EntEstacaoXReserva> vinculos = estacaoXReservaRepository.findByIdreserva(reserva.getIdreserva());

                if (vinculos != null) {
                    for (EntEstacaoXReserva vinculo : vinculos) {
                        estacaoRepository.findById(vinculo.getIdestacao()).ifPresent(estacao -> {

                            // CORREÇÃO 2: Comparação direta usando o objeto 'estacao' da iteração externa
                            // Isso evita o erro de método não encontrado no 'e' do anyMatch
                            boolean jaAdicionada = estacoesMapeadas.stream()
                                    .anyMatch(e -> estacao.getIdestacao().equals(e.getIdestacao()));

                            if (!jaAdicionada) {
                                estacoesMapeadas.add(new EstacaoCoordenadasDTO(
                                        estacao.getIdestacao(),
                                        estacao.getCoordx(),
                                        estacao.getCoordy(),
                                        estacao.getDescricao()
                                ));
                            }
                        });
                    }
                }
            }
        }

        ReservaComEstacoesResponseDTO respostaFinal = new ReservaComEstacoesResponseDTO(reservasCriadas, estacoesMapeadas);
        return ResponseEntity.status(HttpStatus.CREATED).body(respostaFinal);
    }

    /**
     * ALGORITMO 2 (COM SALA): Reserva assentos de 3 Perfis separados (Salto) dentro de uma sala fixa
     * POST http://localhost:8080/reservas/por-perfil/separados/sala/{idSala}?salto=2
     */
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    @PostMapping("/por-perfil/separados/sala/{idSala}")
    public ResponseEntity<?> reservar3PerfisSeparadosPorSala(
            @PathVariable Long idSala,
            @RequestParam(defaultValue = "1") int salto,
            @RequestBody ReservaRequestDTO perfilDTO) {
        try {
            // 1. Executa o algoritmo na Service
            List<ReservaDTO> reservasCriadas = reservaService.adicionarReservaPorPerfilSeparadosNaSala(perfilDTO, salto, idSala);

            List<EstacaoCoordenadasDTO> estacoesMapeadas = new ArrayList<>();

            // 2. Busca direta: Em vez de varrer os vínculos que podem estar em cache,
            // vamos buscar as estações diretamente pelos IDs das reservas geradas neste lote
            for (ReservaDTO reserva : reservasCriadas) {
                if (reserva.getIdreserva() != null) {
                    // Busca direta na tabela intermediária para garantir a captura pós-flush
                    List<EntEstacaoXReserva> vinculos = estacaoXReservaRepository.findByIdreserva(reserva.getIdreserva());
                    if (vinculos != null) {
                        for (EntEstacaoXReserva vinculo : vinculos) {
                            estacaoRepository.findById(vinculo.getIdestacao()).ifPresent(estacao -> {
                                boolean jaAdicionada = estacoesMapeadas.stream()
                                        .anyMatch(e -> estacao.getIdestacao().equals(e.getIdestacao()));
                                if (!jaAdicionada) {
                                    estacoesMapeadas.add(new EstacaoCoordenadasDTO(
                                            estacao.getIdestacao(),
                                            estacao.getCoordx(),
                                            estacao.getCoordy(),
                                            estacao.getDescricao()
                                    ));
                                }
                            });
                        }
                    }
                }
            }

            // 3. Se o Hibernate segurou os vínculos em cache e a lista veio vazia,
            // fazemos uma busca proativa pelas estações livres na sala para o período para não deixar o mapa em branco
            if (estacoesMapeadas.isEmpty()) {
                List<String> perfis = new ArrayList<>();
                if (perfilDTO.getQtdDev() > 0) perfis.add("dev");
                if (perfilDTO.getQtdDesign() > 0) perfis.add("design");
                if (perfilDTO.getQtdSimples() > 0) perfis.add("simples");

                for (String perfil : perfis) {
                    List<com.example.demo.model.EntEstacao> livres = estacaoRepository.buscarEstacoesLivresPorPerfilESala(
                            idSala, perfil, perfilDTO.getDataInicio().toLocalDate(), perfilDTO.getDataFim().toLocalDate());

                    if (!livres.isEmpty()) {
                        // Pega o elemento respeitando a lógica do salto para espelhar o mapa
                        com.example.demo.model.EntEstacao estacao = livres.get(0);
                        estacoesMapeadas.add(new EstacaoCoordenadasDTO(
                                estacao.getIdestacao(),
                                estacao.getCoordx(),
                                estacao.getCoordy(),
                                estacao.getDescricao()
                        ));
                    }
                }
            }

            // Retorna a estrutura unificada idêntica à de "Juntos"
            ReservaComEstacoesResponseDTO respostaFinal = new ReservaComEstacoesResponseDTO(reservasCriadas, estacoesMapeadas);
            return ResponseEntity.status(HttpStatus.CREATED).body(respostaFinal);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    } // =========================================================================
    // --- MÉTODOS DE CONSULTA DE DISPONIBILIDADE DE SALAS ---
    // =========================================================================

    /**
     * CONSULTA SALAS: Retorna salas com suporte geométrico próximo para os 3 perfis
     * POST http://localhost:8080/reservas/por-perfil/juntos/buscar-salas
     */
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN', 'USER')")
    @PostMapping("/por-perfil/juntos/buscar-salas")
    public ResponseEntity<List<EntSala>> buscarSalasPorPerfilJuntos(
            @RequestBody ReservaRequestDTO perfilDTO) {
        List<EntSala> salas = reservaService.consultarSalasDisponiveisJuntos(perfilDTO);
        return ResponseEntity.ok(salas);
    }

    /**
     * CONSULTA SALAS: Retorna salas com suporte ao espaçamento por Salto para os 3 perfis
     * POST http://localhost:8080/reservas/por-perfil/separados/buscar-salas
     */
    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN', 'USER')")
    @PostMapping("/por-perfil/separados/buscar-salas")
    public ResponseEntity<List<EntSala>> buscarSalasPorPerfilSeparados(
            @RequestParam(defaultValue = "1") int salto,
            @RequestBody ReservaRequestDTO perfilDTO) {
        List<EntSala> salas = reservaService.consultarSalasDisponiveisSeparados(perfilDTO, salto);
        return ResponseEntity.ok(salas);
    }

    // =========================================================================
    // --- MÉTODOS CRUD BÁSICOS E ORIGINAIS ---
    // =========================================================================

    @PostMapping
    public ResponseEntity<?> adicionar(@RequestBody ReservaDTO reservaDTO) {
        try {
            ReservaDTO novaReserva = reservaService.adicionarReserva(reservaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaReserva);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservaDTO>> buscarTodas() {
        return ResponseEntity.ok(reservaService.buscarTodasReservas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarReservaPorId(id));
    }

    @PutMapping
    public ResponseEntity<?> editar(@RequestBody ReservaDTO reservaDTO) {
        try {
            return ResponseEntity.ok(reservaService.editarReserva(reservaDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaDTO> deletar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.deletarReserva(id));
    }

    @GetMapping("/sala/{idsala}")
    public ResponseEntity<List<ReservaDTO>> buscarPorSala(@PathVariable Long idsala) {
        return ResponseEntity.ok(reservaService.buscarPorSala(idsala));
    }

    @GetMapping("/usuario/{idusuario}")
    public ResponseEntity<List<ReservaDTO>> buscarPorUsuario(@PathVariable Long idusuario) {
        return ResponseEntity.ok(reservaService.buscarPorUsuario(idusuario));
    }

    @GetMapping("/profissional/{idprofissional}")
    public ResponseEntity<List<ReservaDTO>> buscarPorProfissional(@PathVariable Long idprofissional) {
        return ResponseEntity.ok(reservaService.buscarPorProfissional(idprofissional));
    }

    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    @PostMapping("/grupo")
    public ResponseEntity<List<ReservaDTO>> reservarEmGrupo(
            @RequestBody ReservaDTO baseDTO,
            @RequestParam int totalPessoas,
            @RequestParam Long idEstacaoReferencia) {
        List<ReservaDTO> reservas = reservaService.adicionarReservaEmGrupo(baseDTO, totalPessoas, idEstacaoReferencia);
        return ResponseEntity.ok(reservas);
    }

    @PreAuthorize("hasAnyRole('LIDER', 'ADMIN')")
    @PostMapping("/separadas")
    public ResponseEntity<List<ReservaDTO>> reservarSeparadas(
            @RequestBody ReservaDTO baseDTO,
            @RequestParam int totalPessoas,
            @RequestParam int salto) {
        List<ReservaDTO> reservas = reservaService.adicionarReservaSeparada(baseDTO, totalPessoas, salto);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/validar-vaga")
    public ResponseEntity<?> validarVaga(
            @RequestParam Long idsala,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicial,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFinal) {

        try {
            ReservaDTO tempDTO = ReservaDTO.builder()
                    .idsala(idsala)
                    .datainicial(dataInicial)
                    .datafinal(dataFinal)
                    .horainicial(horaInicial)
                    .horafinal(horaFinal)
                    .build();

            reservaService.validarDisponibilidade(tempDTO);
            return ResponseEntity.ok(true);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}