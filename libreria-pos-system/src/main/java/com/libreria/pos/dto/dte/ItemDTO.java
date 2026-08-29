package com.libreria.pos.dto.dte;

import lombok.Data;
import java.util.List;

@Data
public class ItemDTO {
    private int numItem;             // Correlativo: 1, 2, 3...
    private int tipoItem = 1;        // 1 = Bienes (Cuadernos, lápices) (Catálogo CAT-011)[cite: 5]
    private String numeroDocumento = null;
    private double cantidad;         // Cantidad vendida
    private String codigo;           // El código de barra o SKU de tu producto
    private String codTributo = null;
    private int uniMedida = 59;      // 59 = Unidad (Catálogo CAT-014)[cite: 5]
    private String descripcion;      // Nombre del producto (Ej: "Cuaderno Espiral 200h")
    private double precioUni;        // Precio unitario CON IVA INCLUIDO
    private double montoDescu = 0.0; // Descuento aplicado a este ítem
    private double ventaNoSuj = 0.0;
    private double ventaExenta = 0.0;
    private double ventaGravada;     // Fórmula: (cantidad * precioUni) - montoDescu
    private List<String> tributos = null; // En Factura Consumidor Final el IVA ya va incluido
    private double psv = 0.0;
    private double noGravado = 0.0;
}