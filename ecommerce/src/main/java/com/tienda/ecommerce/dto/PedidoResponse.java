package com.tienda.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponse {
    private Long idPedido;
    private Double total;
    private String estado;
    private LocalDateTime fecha;

    // Datos del Cliente
    private String usuarioEmail;
    private String nombreCliente;
    private String telefonoCliente;

    // Datos de Logística
    private String metodoEntrega;
    private String direccion;
    private Double costoEnvio;

    private List<ProductoResumenDTO> productos;
}