package com.libreria.pos.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoDetallePageResponse {
    private Long idPedido;
    private LocalDateTime fecha;
    private String estado;
    private Double total;
    private Double costoEnvio;
    private String metodoEntrega;
    private String direccion;
    private String codigoQr;
    private List<PedidoDetalleResponse> items;
}
