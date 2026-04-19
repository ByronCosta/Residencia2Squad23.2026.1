package com.example.demo.repository;

import com.example.demo.model.EntEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipamentoRepository extends JpaRepository<EntEquipamento, Long> {
    List<EntEquipamento> findByEstoque(Boolean temEstoque);
}
