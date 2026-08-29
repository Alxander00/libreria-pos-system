package com.libreria.pos.dto.dte;

import lombok.Data;

@Data
public class PagoDTO {
    private String codigo;       // "01" = Billetes/Monedas, "02" = Tarjeta (Catálogo CAT-017)
    private double montoPago;    // Monto pagado con este método
    private String referencia;   // null para efectivo, número de voucher para tarjeta
    private String plazo = null; // null si es de contado
    private Integer periodo = null; // null si es de contado
}