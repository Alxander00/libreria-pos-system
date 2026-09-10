package com.libreria.pos.service.impl;

import com.libreria.pos.dto.AbonoRequest;
import com.libreria.pos.dto.ApartadoRequest;
import com.libreria.pos.dto.ApartadoResponse;
import com.libreria.pos.entities.*;
import com.libreria.pos.repository.ApartadoPagoRepository;
import com.libreria.pos.repository.ApartadoRepository;
import com.libreria.pos.repository.PedidoRepository;
import com.libreria.pos.repository.ProductoRepository;
import com.libreria.pos.service.AuthService;
import com.libreria.pos.service.IApartado;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApartadoImpl implements IApartado {

    @Autowired
    private ApartadoRepository apartadoRepository;

    @Autowired
    private ApartadoPagoRepository pagoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private AuthService authService;

    @Override
    @Transactional
    public ApartadoResponse crearApartado(ApartadoRequest request) {
        UsuarioEntity cliente = authService.getUsuarioAutenticado();

        ProductoEntity producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoVariacionEntity variacion = null;
        if (request.getIdVariacion() != null) {
            variacion = producto.getVariaciones().stream()
                    .filter(v -> v.getIdVariacion().equals(request.getIdVariacion()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Variación no encontrada"));
        } else {
            if (!producto.getVariaciones().isEmpty()) {
                variacion = producto.getVariaciones().get(0);
            }
        }

        if (variacion == null) {
            throw new RuntimeException("El producto no tiene variación configurada");
        }

        // Validar stock disponible
        if (variacion.getStock() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + variacion.getStock());
        }

        // ✅ DESCONTAR STOCK DEFINITIVAMENTE (el cliente se lleva el producto)
        variacion.setStock(variacion.getStock() - request.getCantidad());

        // Precio con descuento
        double precioUnitario = producto.getPrecio();
        if (producto.getDescuento() > 0) {
            precioUnitario = precioUnitario - (precioUnitario * (producto.getDescuento() / 100.0));
        }

        Double total = precioUnitario * request.getCantidad();

        // 1. Crear el pedido asociado (ENTREGADO porque el producto ya se llevó)
        PedidoEntity pedido = new PedidoEntity();
        pedido.setUsuario(cliente);
        pedido.setTotal(total);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.ENTREGADO); // ✅ El producto ya fue entregado
        pedido.setMetodoEntrega("RETIRO");
        pedido.setDireccion("Retiro en Sucursal - Apartado #" + (apartadoRepository.count() + 1));
        pedido.setCostoEnvio(0.0);
        pedido.setMetodoPago(request.getMetodoPagoInicial() != null ? request.getMetodoPagoInicial() : "EFECTIVO");
        pedido.setEsApartado(true); // ✅ Marcamos como apartado para no descontar stock en PedidoImpl

        // Agregar detalle del pedido
        PedidoDetalleEntity detalle = new PedidoDetalleEntity();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setVariacion(variacion);
        detalle.setCantidad(request.getCantidad());
        detalle.setPrecio(precioUnitario);
        pedido.getItems().add(detalle);

        pedidoRepository.save(pedido);

        // 2. Crear el apartado
        ApartadoEntity apartado = new ApartadoEntity();
        apartado.setCliente(cliente);
        apartado.setProducto(producto);
        apartado.setVariacion(variacion);
        apartado.setCantidad(request.getCantidad());
        apartado.setTotalAcordado(total);
        apartado.setSaldoPendiente(total);
        apartado.setFechaCreacion(LocalDateTime.now());
        apartado.setEstado(EstadoApartado.ACTIVO);
        apartado.setPedido(pedido); // ✅ Asociar el pedido

        apartadoRepository.save(apartado);

        // 3. Registrar el abono inicial
        if (request.getMontoInicial() != null && request.getMontoInicial() > 0) {
            if (request.getMontoInicial() > total) {
                throw new RuntimeException("El abono inicial no puede ser mayor al total");
            }
            ApartadoPagoEntity pago = new ApartadoPagoEntity();
            pago.setApartado(apartado);
            pago.setMonto(request.getMontoInicial());
            pago.setFecha(LocalDateTime.now());
            pago.setMetodoPago(request.getMetodoPagoInicial() != null ? request.getMetodoPagoInicial() : "EFECTIVO");
            pagoRepository.save(pago);

            apartado.setMontoPagado(request.getMontoInicial());
            apartado.setSaldoPendiente(total - request.getMontoInicial());
            if (apartado.getSaldoPendiente() <= 0) {
                apartado.setEstado(EstadoApartado.LIQUIDADO);
            }
            apartadoRepository.save(apartado);
        }

        return mapToResponse(apartado);
    }

    @Override
    @Transactional
    public ApartadoResponse cancelar(Long idApartado) {
        ApartadoEntity apartado = apartadoRepository.findById(idApartado)
                .orElseThrow(() -> new RuntimeException("Apartado no encontrado"));

        if (apartado.getEstado() == EstadoApartado.LIQUIDADO) {
            throw new RuntimeException("No se puede cancelar un apartado liquidado");
        }

        // ✅ DEVOLVER STOCK (porque el cliente devuelve el producto)
        ProductoVariacionEntity variacion = apartado.getVariacion();
        if (variacion != null) {
            variacion.setStock(variacion.getStock() + apartado.getCantidad());
        }

        // ✅ Anular el pedido asociado (cambiar estado a CANCELADO)
        PedidoEntity pedido = apartado.getPedido();
        if (pedido != null) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedido);
        }

        apartado.setEstado(EstadoApartado.CANCELADO);
        apartadoRepository.save(apartado);
        return mapToResponse(apartado);
    }

    @Override
    @Transactional
    public ApartadoResponse liquidar(Long idApartado) {
        ApartadoEntity apartado = apartadoRepository.findById(idApartado)
                .orElseThrow(() -> new RuntimeException("Apartado no encontrado"));

        if (apartado.getEstado() != EstadoApartado.ACTIVO) {
            throw new RuntimeException("El apartado ya no está activo");
        }

        // ✅ NO TOCAR STOCK (el producto ya fue entregado)
        // Solo registrar el pago del saldo pendiente
        Double saldoRestante = apartado.getSaldoPendiente();
        if (saldoRestante > 0) {
            ApartadoPagoEntity pago = new ApartadoPagoEntity();
            pago.setApartado(apartado);
            pago.setMonto(saldoRestante);
            pago.setFecha(LocalDateTime.now());
            pago.setMetodoPago("EFECTIVO");
            pagoRepository.save(pago);

            apartado.setMontoPagado(apartado.getTotalAcordado());
            apartado.setSaldoPendiente(0.0);
        }

        apartado.setEstado(EstadoApartado.LIQUIDADO);
        apartadoRepository.save(apartado);

        return mapToResponse(apartado);
    }

    @Override
    public ApartadoResponse abonar(Long idApartado, AbonoRequest request) {
        ApartadoEntity apartado = apartadoRepository.findById(idApartado)
                .orElseThrow(() -> new RuntimeException("Apartado no encontrado"));

        if (apartado.getEstado() != EstadoApartado.ACTIVO) {
            throw new RuntimeException("Solo se pueden abonar apartados activos");
        }

        if (request.getMonto() > apartado.getSaldoPendiente()) {
            throw new RuntimeException("El abono no puede ser mayor al saldo pendiente");
        }

        ApartadoPagoEntity pago = new ApartadoPagoEntity();
        pago.setApartado(apartado);
        pago.setMonto(request.getMonto());
        pago.setFecha(LocalDateTime.now());
        pago.setMetodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : "EFECTIVO");
        pagoRepository.save(pago);

        apartado.setMontoPagado(apartado.getMontoPagado() + request.getMonto());
        apartado.setSaldoPendiente(apartado.getSaldoPendiente() - request.getMonto());

        if (apartado.getSaldoPendiente() <= 0) {
            apartado.setEstado(EstadoApartado.LIQUIDADO);
        }

        apartadoRepository.save(apartado);
        return mapToResponse(apartado);
    }

    @Override
    public List<ApartadoResponse> obtenerMisApartados() {
        UsuarioEntity cliente = authService.getUsuarioAutenticado();
        return apartadoRepository.findByCliente(cliente)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ApartadoResponse> obtenerTodosApartados() {
        List<ApartadoEntity> activos = apartadoRepository.findByEstadoOrderByFechaCreacionDesc(EstadoApartado.ACTIVO);
        List<ApartadoEntity> liquidados = apartadoRepository.findByEstadoOrderByFechaCreacionDesc(EstadoApartado.LIQUIDADO);
        List<ApartadoEntity> cancelados = apartadoRepository.findByEstadoOrderByFechaCreacionDesc(EstadoApartado.CANCELADO);

        List<ApartadoEntity> todos = new ArrayList<>();
        todos.addAll(activos);
        todos.addAll(liquidados);
        todos.addAll(cancelados);
        return todos.stream().map(this::mapToResponse).toList();
    }

    // ========== MAPPER ==========
    private ApartadoResponse mapToResponse(ApartadoEntity a) {
        ApartadoResponse res = new ApartadoResponse();
        res.setIdApartado(a.getIdApartado());
        res.setNombreCliente(a.getCliente().getNombre());
        res.setEmailCliente(a.getCliente().getEmail());
        res.setNombreProducto(a.getProducto().getNombre());

        String varName = "Único";
        if (a.getVariacion() != null) {
            String color = a.getVariacion().getColor() != null && !"Único".equals(a.getVariacion().getColor()) ? a.getVariacion().getColor() : "";
            String talla = a.getVariacion().getTalla() != null && !"Única".equals(a.getVariacion().getTalla()) ? a.getVariacion().getTalla() : "";
            if (!color.isEmpty() && !talla.isEmpty()) varName = color + " - " + talla;
            else if (!color.isEmpty()) varName = color;
            else if (!talla.isEmpty()) varName = talla;
        }
        res.setVariacionNombre(varName);
        res.setCantidad(a.getCantidad());
        res.setTotalAcordado(a.getTotalAcordado());
        res.setMontoPagado(a.getMontoPagado());
        res.setSaldoPendiente(a.getSaldoPendiente());
        res.setEstado(a.getEstado().name());
        res.setFechaCreacion(a.getFechaCreacion());

        List<ApartadoResponse.PagoResponse> pagos = a.getPagos().stream().map(p -> {
            ApartadoResponse.PagoResponse pr = new ApartadoResponse.PagoResponse();
            pr.setMonto(p.getMonto());
            pr.setFecha(p.getFecha());
            pr.setMetodoPago(p.getMetodoPago());
            return pr;
        }).toList();
        res.setPagos(pagos);
        return res;
    }
}