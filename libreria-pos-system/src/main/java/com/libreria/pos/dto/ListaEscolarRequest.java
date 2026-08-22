package com.libreria.pos.dto;

import lombok.Data;
import java.util.List;

@Data
public class ListaEscolarRequest {
    private String grado;
    private String anio;
    private List<ItemLista> items;

    @Data
    public static class ItemLista {
        private Long idProducto;
        private Long idVariacion; // Puede ser null si es producto único
        private Long cantidad;
    }
}