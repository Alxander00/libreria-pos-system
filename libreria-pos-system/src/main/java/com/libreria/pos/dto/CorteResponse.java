package com.libreria.pos.dto;

import lombok.Data;

@Data
public class CorteResponse {
    private String mensaje;
    private Long idCorte;
    private Double totalGeneral;
    private Double efectivoEnCaja;
    private Double diferencia;
}