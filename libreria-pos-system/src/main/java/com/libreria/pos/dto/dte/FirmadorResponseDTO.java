package com.libreria.pos.dto.dte;

import lombok.Data;

@Data
public class FirmadorResponseDTO {
    private String status; // Devolverá "OK" o "ERROR"
    private String body;   // Si es "OK", aquí viene tu JSON final ya firmado y listo para Hacienda
}