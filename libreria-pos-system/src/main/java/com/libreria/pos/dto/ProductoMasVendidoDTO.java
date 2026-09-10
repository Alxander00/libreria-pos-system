package com.libreria.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoMasVendidoDTO {
    private Long idProducto;
    private String nombre;
    private Long cantidadVendida;
    private Double montoTotal;
    private String categoria;
}