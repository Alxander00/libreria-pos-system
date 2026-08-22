package com.libreria.pos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "apartado_pagos")
public class ApartadoPagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    @ManyToOne
    @JoinColumn(name = "id_apartado", nullable = false)
    private ApartadoEntity apartado;

    private Double monto;

    private LocalDateTime fecha;

    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA
}