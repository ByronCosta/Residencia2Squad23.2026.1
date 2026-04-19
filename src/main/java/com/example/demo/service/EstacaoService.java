package com.example.demo.service;

import com.example.demo.dto.EstacaoDTO;
import com.example.demo.model.EntEstacao;
import com.example.demo.repository.EstacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstacaoService {

    @Autowired
    private EstacaoRepository estacaoRepository;

    // 1. Adicionar Estação
    public EstacaoDTO adicionarEstacao(EstacaoDTO estacaoDTO) {
        EntEstacao entEstacao = EntEstacao.builder()
                .idestacao(estacaoDTO.getIdestacao())
                .descricao(estacaoDTO.getDescricao())
                .build();

        // Corrigido: Removido o save duplicado e ajustado o nome da variável
        EntEstacao estacaoSalva = estacaoRepository.save(entEstacao);
        return mapToDTO(estacaoSalva);
    }

    // 2. Remover Estação
    public void removerEstacao(Long idestacao) {
        estacaoRepository.deleteById(idestacao);
    }

    // 3. Buscar Estação por ID
    public EstacaoDTO buscarPorId(Long idestacao) {
        // Corrigido: 'idsala' para 'idestacao' na mensagem de erro
        EntEstacao estacao = estacaoRepository.findById(idestacao)
                .orElseThrow(() -> new RuntimeException("Estação não encontrada com ID: " + idestacao));
        return mapToDTO(estacao);
    }

    // 4. Listar Todas as Estações
    public List<EstacaoDTO> listarTodos() {
        return estacaoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 5. Editar Estação
    public EstacaoDTO editarEstacao(EstacaoDTO estacaoDTO) {
        // Verifica se a estação existe
        EntEstacao estacaoExistente = estacaoRepository.findById(estacaoDTO.getIdestacao())
                .orElseThrow(() -> new RuntimeException("Estação não encontrada com ID: " + estacaoDTO.getIdestacao()));

        // Atualiza a descrição
        estacaoExistente.setDescricao(estacaoDTO.getDescricao());

        // Salva a alteração
        EntEstacao estacaoAtualizada = estacaoRepository.save(estacaoExistente);
        return mapToDTO(estacaoAtualizada);
    }

    // Método Auxiliar de Mapeamento
    private EstacaoDTO mapToDTO(EntEstacao estacao) {
        return EstacaoDTO.builder()
                .idestacao(estacao.getIdestacao())
                .descricao(estacao.getDescricao())
                .build();
    }
}