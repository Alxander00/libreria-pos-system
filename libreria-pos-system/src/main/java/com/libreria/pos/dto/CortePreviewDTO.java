package com.libreria.pos.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CortePreviewDTO {
    private Double totalEfectivo;
    private Double totalTarjeta;
    private Double totalTransferencia;
    private Double totalGeneral;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}