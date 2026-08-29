package com.libreria.pos.dto.dte;

import lombok.Data;

@Data
public class IdentificacionDTO {
    private int version = 2;               // Versión del JSON (2 para Factura)
    private String ambiente;               // "00" para Pruebas, "01" para Producción
    private String tipoDte = "01";         // "01" es Factura, "03" es Crédito Fiscal
    private String numeroControl;          // Ej: DTE-01-M001P025-000000000000001
    private String codigoGeneracion;       // UUID v4 generado automáticamente
    private int tipoModelo = 1;            // 1 = Previo, 2 = Diferido
    private int tipoOperacion = 1;         // 1 = Normal
    private String fecEmi;                 // Fecha formato YYYY-MM-DD
    private String horEmi;                 // Hora formato HH:mm:ss
    private String tipoMoneda = "USD";     // USD por defecto
}