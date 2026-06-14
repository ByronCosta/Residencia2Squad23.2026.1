package com.example.demo.repository;

import com.example.demo.model.EntProfissionaisXReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProfissionaisXReservaRepository extends JpaRepository<EntProfissionaisXReserva, Long> {
    List<EntProfissionaisXReserva> findByIdprofissional(Long idprofissional);
    List<EntProfissionaisXReserva> findByIdreserva(Long idreserva);
}