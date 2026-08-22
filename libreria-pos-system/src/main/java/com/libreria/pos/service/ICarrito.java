package com.libreria.pos.service;

import com.libreria.pos.entities.CarritoEntity;

public interface ICarrito {

    CarritoEntity obtenerCarritoUsuario();
    CarritoEntity agregarProducto(Long idProducto, Long cantidad, Long idVariacion);
    CarritoEntity eliminarProducto(Long idProducto);
    CarritoEntity aumentarCantidad(Long idProducto);
    CarritoEntity disminuirCantidad(Long idProducto);
    void vaciarCarrito();
}
