package com.example.demo.repository;

import com.example.demo.model.EntEstacaoXReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstacaoXReservaRepository extends JpaRepository<EntEstacaoXReserva, Long> {

    // Busca todos os vínculos de uma estação específica
    List<EntEstacaoXReserva> findByIdestacao(Long idestacao);

    // Busca todos os vínculos de uma reserva específica
    List<EntEstacaoXReserva> findByIdreserva(Long idreserva);
}
