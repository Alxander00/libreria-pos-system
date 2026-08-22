package com.tienda.ecommerce.dto;

import lombok.Data;

@Data
public class PedidoDetalleResponse {

    private String producto;
    private Double precio;
    private Long cantidad;
    private Double subtotal;
}
