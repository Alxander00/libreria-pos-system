package com.tienda.ecommerce.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {
    private String nombre;
    private String email;
    private String password;
    private String direccion;
    private String telefono;
    private String rol;
}
