package com.libreria.pos.dto.dte;

import lombok.Data;

@Data
public class FirmadorRequestDTO {
    private String nit;           // Tu NIT sin guiones (Ej: "0210...")
    private boolean activo = true;// Siempre true
    private String passwordPri;   // La contraseña de tu certificado (llave privada)
    private String dteJson;       // Aquí enviaremos la FacturaElectronicaDTO convertida a texto
}