package com.libreria.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteFrecuenteDTO {
    private Long idUsuario;
    private String nombre;
    private String email;
    private Long totalPedidos;
    private Double montoGastado;
}