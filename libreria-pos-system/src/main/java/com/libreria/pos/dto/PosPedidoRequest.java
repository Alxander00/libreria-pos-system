package com.libreria.pos.dto;

import lombok.Data;
import java.util.List;

@Data
public class PosPedidoRequest {
    private List<ItemPos> items;
    private String metodoPago; // "EFECTIVO" o "TARJETA"

    @Data
    public static class ItemPos {
        private Long idProducto;
        private Long idVariacion; // Puede ser null si es producto único
        private Long cantidad;
    }
}