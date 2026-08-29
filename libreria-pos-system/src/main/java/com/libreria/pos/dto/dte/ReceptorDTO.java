package com.libreria.pos.dto.dte;

import lombok.Data;

@Data
public class ReceptorDTO {
    private String tipoDocumento; // "13" para DUI, "36" para NIT, "37" Otro (Catálogo CAT-022)
    private String numDocumento;  // El número de DUI o NIT sin guiones
    private String nrc;           // Normalmente null para Factura Consumidor Final
    private String nombre;        // Nombre completo del cliente
    private String codActividad;  // null
    private String descActividad; // null
    private DireccionDTO direccion; // Puede ser null si no lo proporciona
    private String telefono;      // null
    private String correo;        // Fundamental para enviarle el PDF y JSON por correo
}