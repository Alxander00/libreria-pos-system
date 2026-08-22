package com.tienda.ecommerce.controller;

import com.tienda.ecommerce.entities.CarritoEntity;
import com.tienda.ecommerce.service.ICarrito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private ICarrito carrito;

    @GetMapping
    public CarritoEntity obtenerCarritoUsuario() {
        return carrito.obtenerCarritoUsuario();
    }

    @PostMapping("/agregar")
    public CarritoEntity agregarAlCarrito(
            @RequestParam Long idProducto,
            @RequestParam Long cantidad,
            @RequestParam(required = false) Long idVariacion // 👇 SE AGREGA ESTO
    ) {
        return carrito.agregarProducto(idProducto, cantidad, idVariacion);
    }

    @DeleteMapping("/eliminar/{idProducto}")
    public CarritoEntity eliminarProducto(@PathVariable Long idProducto) {
        return carrito.eliminarProducto(idProducto);
    }

    @PutMapping("/aumentar/{idProducto}")
    public CarritoEntity aumentar(@PathVariable Long idProducto) {
        return carrito.aumentarCantidad(idProducto);
    }

    @PutMapping("/disminuir/{idProducto}")
    public CarritoEntity disminuir(@PathVariable Long idProducto) {
        return carrito.disminuirCantidad(idProducto);
    }

    @DeleteMapping("/vaciar")
    public void vaciarCarrito() {
        carrito.vaciarCarrito();
    }
}
