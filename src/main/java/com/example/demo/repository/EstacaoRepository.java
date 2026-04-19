package com.example.demo.repository;


import com.example.demo.model.EntEstacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstacaoRepository extends JpaRepository<EntEstacao, Long> {

}
