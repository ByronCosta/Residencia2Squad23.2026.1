package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime; // Sugestão: use LocalTime em vez de java.sql.Time

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estacao")
public class EntEstacao{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idestacao")
    private Long idestacao;
    @Column(name = "descricao")
    private String descricao;


}
