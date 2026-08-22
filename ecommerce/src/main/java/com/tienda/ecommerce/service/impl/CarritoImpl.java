package com.tienda.ecommerce.service.impl;

import com.tienda.ecommerce.entities.CarritoDetalleEntity;
import com.tienda.ecommerce.entities.CarritoEntity;
import com.tienda.ecommerce.entities.ProductoEntity;
import com.tienda.ecommerce.entities.UsuarioEntity;
import com.tienda.ecommerce.repository.*;
import com.tienda.ecommerce.service.ICarrito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.tienda.ecommerce.entities.ProductoVariacionEntity;

@Service
public class CarritoImpl implements ICarrito {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private CarritoDetalleRepository detalleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public CarritoEntity obtenerCarritoUsuario() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return carritoRepository.findByUsuario(usuario)
                .orElseGet(() -> {
                    CarritoEntity carrito = new CarritoEntity();
                    carrito.setUsuario(usuario);
                    return carritoRepository.save(carrito);
                });
    }

    @Override
    public CarritoEntity agregarProducto(Long idProducto, Long cantidad, Long idVariacion) {

        CarritoEntity carrito = obtenerCarritoUsuario();

        ProductoEntity producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no existe"));

        // 👇 1. BUSCAMOS QUÉ COLOR ELIGIÓ EL CLIENTE 👇
        ProductoVariacionEntity variacionElegida = null;
        if (idVariacion != null) {
            variacionElegida = producto.getVariaciones().stream()
                    .filter(v -> v.getIdVariacion().equals(idVariacion))
                    .findFirst()
                    .orElse(null);
        }

        for (CarritoDetalleEntity item : carrito.getItems()) {
            if (item.getProducto().getIdProducto().equals(idProducto)) {

                // 👇 2. VERIFICAMOS SI ES EXACTAMENTE EL MISMO COLOR 👇
                boolean mismoColor = false;
                if (idVariacion == null && item.getVariacion() == null) mismoColor = true;
                if (idVariacion != null && item.getVariacion() != null && item.getVariacion().getIdVariacion().equals(idVariacion)) mismoColor = true;

                if (mismoColor) {
                    Long nuevaCantidad = item.getCantidad() + cantidad;

                    // Si la variación tiene stock propio, validamos contra ese stock
                    Long stockDisponible = (variacionElegida != null) ? variacionElegida.getStock() : producto.getStock();

                    if (nuevaCantidad > stockDisponible) {
                        throw new RuntimeException("Stock insuficiente para este color.");
                    }

                    item.setCantidad(nuevaCantidad);
                    return carritoRepository.save(carrito);
                }
            }
        }

        // 👇 3. SI NO EXISTÍA EN EL CARRITO, LO AGREGAMOS CON SU COLOR 👇
        Long stockDisp = (variacionElegida != null) ? variacionElegida.getStock() : producto.getStock();
        if (cantidad > stockDisp) {
            throw new RuntimeException("Stock insuficiente.");
        }

        CarritoDetalleEntity nuevo = new CarritoDetalleEntity();
        nuevo.setCarrito(carrito);
        nuevo.setProducto(producto);
        nuevo.setCantidad(cantidad);
        nuevo.setVariacion(variacionElegida); // ¡EL ESLABÓN PERDIDO!

        carrito.getItems().add(nuevo);

        return carritoRepository.save(carrito);
    }

    @Override
    public CarritoEntity eliminarProducto(Long idProducto) {
        CarritoEntity carrito = obtenerCarritoUsuario();

        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            return carrito;
        }

        carrito.getItems().removeIf(item -> item.getProducto().getIdProducto().equals(idProducto));

        return carritoRepository.save(carrito);
    }

    @Override
    public CarritoEntity aumentarCantidad(Long idProducto) {

        CarritoEntity carrito = obtenerCarritoUsuario();

        for (CarritoDetalleEntity item : carrito.getItems()) {
            if (item.getProducto().getIdProducto().equals(idProducto)) {

                if (item.getCantidad() + 1 > item.getProducto().getStock()) {
                    throw new RuntimeException("Stock insuficiente");
                }

                item.setCantidad(item.getCantidad() + 1);
                break;
            }
        }

        return carritoRepository.save(carrito);
    }

    @Override
    public CarritoEntity disminuirCantidad(Long idProducto) {

        CarritoEntity carrito = obtenerCarritoUsuario();

        carrito.getItems().removeIf(item -> {
            if (item.getProducto().getIdProducto().equals(idProducto)) {
                if (item.getCantidad() > 1) {
                    item.setCantidad(item.getCantidad() - 1);
                    return false;
                }
                return true; // elimina si queda en 0
            }
            return false;
        });

        return carritoRepository.save(carrito);
    }

    @Override
    public void vaciarCarrito() {

        CarritoEntity carrito = obtenerCarritoUsuario();

        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

}

