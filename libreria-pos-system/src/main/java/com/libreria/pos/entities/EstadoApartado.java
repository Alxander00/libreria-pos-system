package com.libreria.pos.entities;

public enum EstadoApartado {
    ACTIVO,    // Cliente está pagando
    LIQUIDADO, // Pagado completo
    CANCELADO  // Cancelado, stock devuelto
}