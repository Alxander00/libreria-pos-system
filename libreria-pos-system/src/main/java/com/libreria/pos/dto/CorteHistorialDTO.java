package com.libreria.pos.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CorteHistorialDTO {
    private Long idCorte;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private Double totalEfectivo;
    private Double totalTarjeta;
    private Double totalTransferencia;
    private Double totalGeneral;
    private Double efectivoEnCaja;
    private Double diferencia;
    private String usuarioCierre;
}