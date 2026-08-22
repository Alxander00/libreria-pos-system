package com.tienda.ecommerce.dto;

import lombok.Data;

@Data
public class DescuentoRequest {
    private String tipo;    // GLOBAL, CATEGORIA o PRODUCTO
    private Integer valor;  // El porcentaje (ej: 15)
    private Long id;        // ID de la categoría o del producto (si aplica)
}