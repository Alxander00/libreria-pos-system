package com.libreria.pos.dto.dte;

import lombok.Data;

@Data
public class DireccionDTO {
    private String departamento; // Código según catálogo (Ej: "01" para Ahuachapán)
    private String municipio;    // Código según catálogo (Ej: "03" para Atiquizaya)
    private String complemento;  // Dirección exacta en texto libre
}