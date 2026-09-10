package com.libreria.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngresosPorCategoriaDTO {
    private String categoria;
    private Double totalIngresos;
    private Long cantidadProductosVendidos;
}