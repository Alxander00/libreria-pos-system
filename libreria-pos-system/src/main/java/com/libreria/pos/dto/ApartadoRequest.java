package com.libreria.pos.dto;

import lombok.Data;

@Data
public class ApartadoRequest {
    private Long idProducto;
    private Long idVariacion; // opcional
    private Long cantidad = 1L;
    private Double montoInicial; // Abono inicial
    private String metodoPagoInicial; // EFECTIVO, TARJETA
}