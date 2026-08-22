package com.libreria.pos.service.impl;

import com.libreria.pos.dto.ListaEscolarRequest;
import com.libreria.pos.dto.ListaEscolarResponse;
import com.libreria.pos.entities.*;
import com.libreria.pos.repository.ListaDetalleRepository;
import com.libreria.pos.repository.ListaEscolarRepository;
import com.libreria.pos.repository.ProductoRepository;
import com.libreria.pos.service.AuthService;
import com.libreria.pos.service.EmailService;
import com.libreria.pos.service.IListaEscolar;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListaEscolarImpl implements IListaEscolar {

    @Autowired
    private ListaEscolarRepository listaRepository;

    @Autowired
    private ListaDetalleRepository detalleRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public ListaEscolarResponse crearLista(ListaEscolarRequest request) {
        UsuarioEntity cliente = authService.getUsuarioAutenticado();

        ListaEscolarEntity lista = new ListaEscolarEntity();
        lista.setUsuario(cliente);
        lista.setGrado(request.getGrado());
        lista.setAnio(request.getAnio());
        lista.setFechaCreacion(LocalDateTime.now());
        lista.setEstado(EstadoLista.PENDIENTE);

        // Procesar items
        for (ListaEscolarRequest.ItemLista itemReq : request.getItems()) {
            ProductoEntity producto = productoRepository.findById(itemReq.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            ProductoVariacionEntity variacion = null;
            if (itemReq.getIdVariacion() != null) {
                variacion = producto.getVariaciones().stream()
                        .filter(v -> v.getIdVariacion().equals(itemReq.getIdVariacion()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Variación no encontrada"));
            } else {
                // Si no especificó variación, usar la primera disponible
                if (!producto.getVariaciones().isEmpty()) {
                    variacion = producto.getVariaciones().get(0);
                }
            }

            ListaDetalleEntity detalle = new ListaDetalleEntity();
            detalle.setLista(lista);
            detalle.setProducto(producto);
            detalle.setVariacion(variacion);
            detalle.setCantidadSolicitada(itemReq.getCantidad());

            lista.getDetalles().add(detalle);
        }

        listaRepository.save(lista);
        return mapToResponse(lista);
    }

    @Override
    public List<ListaEscolarResponse> obtenerMisListas() {
        UsuarioEntity cliente = authService.getUsuarioAutenticado();
        return listaRepository.findByUsuario(cliente)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<ListaEscolarResponse> obtenerTodasLasListas() {
        // Ordenar por pendientes primero, luego armadas, luego retiradas
        List<ListaEscolarEntity> pendientes = listaRepository.findByEstadoOrderByFechaCreacionDesc(EstadoLista.PENDIENTE);
        List<ListaEscolarEntity> armadas = listaRepository.findByEstadoOrderByFechaCreacionDesc(EstadoLista.ARMADO);
        List<ListaEscolarEntity> retiradas = listaRepository.findByEstadoOrderByFechaCreacionDesc(EstadoLista.RETIRADO);

        List<ListaEscolarEntity> todas = new ArrayList<>();
        todas.addAll(pendientes);
        todas.addAll(armadas);
        todas.addAll(retiradas);

        return todas.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public ListaEscolarResponse armarLista(Long idLista) {
        ListaEscolarEntity lista = listaRepository.findById(idLista)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        if (lista.getEstado() != EstadoLista.PENDIENTE) {
            throw new RuntimeException("Solo se pueden armar listas en estado PENDIENTE");
        }

        // Descontar stock de cada producto
        for (ListaDetalleEntity detalle : lista.getDetalles()) {
            ProductoVariacionEntity variacion = detalle.getVariacion();
            if (variacion != null) {
                if (variacion.getStock() < detalle.getCantidadSolicitada()) {
                    throw new RuntimeException("Stock insuficiente para " + detalle.getProducto().getNombre() +
                            ". Disponible: " + variacion.getStock());
                }
                variacion.setStock(variacion.getStock() - detalle.getCantidadSolicitada());
            } else {
                // Si no tiene variación, usar stock del producto (pero debería tener variación)
                throw new RuntimeException("El producto no tiene variación configurada");
            }
        }

        lista.setEstado(EstadoLista.ARMADO);
        listaRepository.save(lista);

        // Enviar correo al cliente
        String mensaje = "Hola " + lista.getUsuario().getNombre() + ",\n\n" +
                "¡Tu lista escolar #" + lista.getIdLista() + " ya está armada y lista para retirar!\n" +
                "Puedes pasar a nuestra tienda en El Tunco, La Libertad, a recogerla.\n\n" +
                "¡Te esperamos!";
        emailService.enviarNotificacion(lista.getUsuario().getEmail(), "📚 Tu lista escolar está lista", mensaje);

        return mapToResponse(lista);
    }

    @Override
    @Transactional
    public ListaEscolarResponse retirarLista(Long idLista) {
        ListaEscolarEntity lista = listaRepository.findById(idLista)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        if (lista.getEstado() != EstadoLista.ARMADO) {
            throw new RuntimeException("Solo se pueden retirar listas en estado ARMADO");
        }

        lista.setEstado(EstadoLista.RETIRADO);
        listaRepository.save(lista);

        return mapToResponse(lista);
    }

    // ========== MAPPER ==========
    private ListaEscolarResponse mapToResponse(ListaEscolarEntity lista) {
        ListaEscolarResponse response = new ListaEscolarResponse();
        response.setIdLista(lista.getIdLista());
        response.setNombreCliente(lista.getUsuario().getNombre());
        response.setEmailCliente(lista.getUsuario().getEmail());
        response.setGrado(lista.getGrado());
        response.setAnio(lista.getAnio());
        response.setEstado(lista.getEstado().name());
        response.setFechaCreacion(lista.getFechaCreacion());

        List<ListaEscolarResponse.DetalleListaResponse> detalles = lista.getDetalles().stream().map(d -> {
            ListaEscolarResponse.DetalleListaResponse det = new ListaEscolarResponse.DetalleListaResponse();
            det.setProductoNombre(d.getProducto().getNombre());

            String variacionNombre = "Único";
            if (d.getVariacion() != null) {
                String color = d.getVariacion().getColor() != null && !"Único".equals(d.getVariacion().getColor()) ? d.getVariacion().getColor() : "";
                String talla = d.getVariacion().getTalla() != null && !"Única".equals(d.getVariacion().getTalla()) ? d.getVariacion().getTalla() : "";
                if (!color.isEmpty() && !talla.isEmpty()) variacionNombre = color + " - " + talla;
                else if (!color.isEmpty()) variacionNombre = color;
                else if (!talla.isEmpty()) variacionNombre = talla;
            }
            det.setVariacionNombre(variacionNombre);
            det.setCantidadSolicitada(d.getCantidadSolicitada());
            det.setPrecioUnitario(d.getProducto().getPrecio());
            return det;
        }).toList();

        response.setDetalles(detalles);
        return response;
    }

    @Override
    public ListaEscolarResponse obtenerLista(Long id) {
        ListaEscolarEntity lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));
        // Validar que el cliente sea el dueño o que sea admin
        UsuarioEntity usuario = authService.getUsuarioAutenticado();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !lista.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No autorizado");
        }
        return mapToResponse(lista);
    }

    @Override
    @Transactional
    public ListaEscolarResponse actualizarLista(Long id, ListaEscolarRequest request) {
        ListaEscolarEntity lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        if (lista.getEstado() != EstadoLista.PENDIENTE) {
            throw new RuntimeException("No se puede modificar una lista que ya está armada o retirada");
        }

        // Validar permisos (cliente dueño o admin)
        UsuarioEntity usuario = authService.getUsuarioAutenticado();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !lista.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No autorizado");
        }

        // Actualizar grado y año
        if (request.getGrado() != null) lista.setGrado(request.getGrado());
        if (request.getAnio() != null) lista.setAnio(request.getAnio());

        // Reemplazar detalles
        lista.getDetalles().clear();
        for (ListaEscolarRequest.ItemLista itemReq : request.getItems()) {
            ProductoEntity producto = productoRepository.findById(itemReq.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            ProductoVariacionEntity variacion = null;
            if (itemReq.getIdVariacion() != null) {
                variacion = producto.getVariaciones().stream()
                        .filter(v -> v.getIdVariacion().equals(itemReq.getIdVariacion()))
                        .findFirst()
                        .orElse(null);
            } else if (!producto.getVariaciones().isEmpty()) {
                variacion = producto.getVariaciones().get(0);
            }
            ListaDetalleEntity detalle = new ListaDetalleEntity();
            detalle.setLista(lista);
            detalle.setProducto(producto);
            detalle.setVariacion(variacion);
            detalle.setCantidadSolicitada(itemReq.getCantidad());
            lista.getDetalles().add(detalle);
        }

        listaRepository.save(lista);
        return mapToResponse(lista);
    }
}