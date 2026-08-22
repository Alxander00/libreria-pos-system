package com.libreria.pos.dto;

import lombok.Data;

@Data
public class AbonoRequest {
    private Double monto;
    private String metodoPago;
}