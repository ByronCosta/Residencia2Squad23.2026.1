package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "salas")
public class EntSala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idsala")
    private Long idsala;

    @Column(name = "endereco")
    private String endereco;

    @Column(name = "disponibilidade")
    private Boolean disponibilidade; // Alterado para Boolean (Wrapper)

    @Column(name = "lot_max")
    private Integer lot_max;
}