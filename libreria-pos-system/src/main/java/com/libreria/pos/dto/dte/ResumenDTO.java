package com.libreria.pos.dto.dte;

import lombok.Data;
import java.util.List;

@Data
public class ResumenDTO {
    private double totalNoSuj = 0.0;
    private double totalExenta = 0.0;
    private double totalGravada;        // Suma de las 'ventaGravada' de todos los items
    private double subTotalVentas;      // Igual a totalGravada
    private double descuNoSuj = 0.0;
    private double descuExenta = 0.0;
    private double descuGravada = 0.0;  // Descuento global aplicado a la factura
    private double porcentajeDescuento = 0.0;
    private double totalDescu = 0.0;    // Suma de descuentos de items + descuento global
    private Object tributos = null;     // null para Factura Consumidor Final
    private double subTotal;            // subTotalVentas - descuGravada
    private double ivaPerci1 = 0.0;
    private double ivaRete1 = 0.0;
    private double reteRenta = 0.0;
    private double montoTotalOperacion; // Igual a subTotal
    private double totalNoGravado = 0.0;
    private double totalPagar;          // Igual a montoTotalOperacion
    private String totalLetras;         // Ej: "CINCO DOLARES CON 50/100"
    private double saldoFavor = 0.0;
    private int condicionOperacion = 1; // 1 = Contado (Catálogo CAT-016)[cite: 5]
    private List<PagoDTO> pagos;        // La lista con la forma en que pagó el cliente
    private String numPagoElectronico = null;
}