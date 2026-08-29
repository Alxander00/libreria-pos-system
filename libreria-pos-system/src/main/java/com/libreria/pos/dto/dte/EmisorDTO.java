package com.libreria.pos.dto.dte;

import lombok.Data;

@Data
public class EmisorDTO {
    private String nit;                 // NIT de la librería sin guiones
    private String nrc;                 // NRC de la librería sin guiones
    private String nombre;              // Razón social o nombre legal
    private String codActividad;        // Código de actividad económica (Catálogo CAT-019)
    private String descActividad;       // Descripción de la actividad económica
    private String nombreComercial;     // Nombre comercial de la librería
    private String tipoEstablecimiento; // Código de sucursal/matriz (Catálogo CAT-009)
    private DireccionDTO direccion;     // Objeto creado en el Paso 2
    private String telefono;            // Teléfono de contacto
    private String correo;              // Correo de la librería
    private String codEstableMH;        // Código asignado por MH (puede ser null para FCF)
    private String codEstable;          // Código interno de tu sucursal
    private String codPuntoVentaMH;     // Código del POS asignado por MH
    private String codPuntoVenta;       // Código interno de tu caja
}