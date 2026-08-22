package com.libreria.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResumenDTO {
    private String nombre;
    private Long cantidad;
    private String imagenUrl;
    private Double precioUnitario;
    private Double subtotal;
    private String color;
    private String talla;
}
