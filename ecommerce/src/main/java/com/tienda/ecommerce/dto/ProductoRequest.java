package com.tienda.ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductoRequest {
    private String nombre;
    private String descripcion;
    private double precio;
    private Long stock;
    private String imagenUrl;
    private Long idCategoria;
    private Long descuento;
}
