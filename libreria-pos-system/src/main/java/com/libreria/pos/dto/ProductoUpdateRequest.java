package com.libreria.pos.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductoUpdateRequest {
    private String nombre;
    private String descripcion;
    private double precio;
    private Long stock;
    private String imagen_url;
    private Long idCategoria;
}
