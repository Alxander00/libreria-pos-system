package com.tienda.ecommerce.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PagoResponse {

    private Long idPago;
    private Long idPedido;
    private String metodoPago;
    private String estado;
    private Double monto;
    private LocalDateTime fecha;

}

