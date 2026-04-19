package com.example.demo.repository;

import com.example.demo.model.EntEquipamento;
import com.example.demo.model.EntEquipamentoXEstacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipamentoXEstacaoRepository extends JpaRepository<EntEquipamentoXEstacao, Long> {
    List<EntEquipamentoXEstacao> findByIdequipamento(Long idequipamento);
    List<EntEquipamentoXEstacao> findByIdestacao(Long idestacao);

}
