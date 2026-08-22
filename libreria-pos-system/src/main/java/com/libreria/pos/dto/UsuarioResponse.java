package com.libreria.pos.dto;

import lombok.Data;

@Data
public class UsuarioResponse {
    private Long idUsuario;
    private String nombre;
    private String email;
    private String direccion;
    private String telefono;
    private String rol;
    private String fotoUrl;
}
