package com.libreria.pos.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ListaEscolarResponse {
    private Long idLista;
    private String nombreCliente;
    private String emailCliente;
    private String grado;
    private String anio;
    private String estado;
    private LocalDateTime fechaCreacion;
    private List<DetalleListaResponse> detalles;

    @Data
    public static class DetalleListaResponse {
        private String productoNombre;
        private String variacionNombre; // "Azul - M" o "Único"
        private Long cantidadSolicitada;
        private Double precioUnitario;
    }
}