package com.libreria.pos.dto.dte;

import lombok.Data;
import java.util.List;

@Data
public class FacturaElectronicaDTO {
    private IdentificacionDTO identificacion; // Las variables de control (Versión, ambiente, UUID)
    private Object documentoRelacionado;      // Para Factura de Consumidor Final suele ir en null
    private EmisorDTO emisor;                 // Los datos de tu librería
    private ReceptorDTO receptor;             // Los datos del cliente (Siguiente paso)
    private Object otrosDocumentos;           // Generalmente null
    private Object ventaTercero;              // Generalmente null
    private List<ItemDTO> cuerpoDocumento;    // Lista de productos del carrito de compras
    private ResumenDTO resumen;               // Totales, IVA y retenciones
    private Object apendice;                  // Generalmente null
}