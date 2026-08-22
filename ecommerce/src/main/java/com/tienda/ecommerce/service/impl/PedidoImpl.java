package com.tienda.ecommerce.service.impl;

import com.tienda.ecommerce.dto.*;
import com.tienda.ecommerce.entities.*;
import com.tienda.ecommerce.repository.*;
import com.tienda.ecommerce.service.AuthService;
import com.tienda.ecommerce.service.EmailService;
import com.tienda.ecommerce.service.IPedido;
import com.tienda.ecommerce.service.ICarrito;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoImpl implements IPedido {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoDetalleRepository detalleRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ICarrito carritoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public PedidoEntity crearPedido(PedidoRequest request) {

        CarritoEntity carrito = carritoService.obtenerCarritoUsuario();

        if (carrito.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        for (CarritoDetalleEntity item : carrito.getItems()) {
            ProductoEntity producto = item.getProducto();
            if (item.getCantidad() > producto.getStock()) {
                throw new RuntimeException("Stock insuficiente para el producto " + producto.getNombre());
            }
        }

        PedidoEntity pedido = new PedidoEntity();
        pedido.setUsuario(carrito.getUsuario());
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        pedido.setMetodoEntrega(request.getMetodoEntrega());
        pedido.setDireccion(request.getDireccion());
        pedido.setCostoEnvio(request.getCostoEnvio());

        Double envio = request.getCostoEnvio() != null ? request.getCostoEnvio() : 0.0;
        pedido.setTotal(carrito.getTotal() + envio);

        pedidoRepository.save(pedido);

        for (CarritoDetalleEntity item : carrito.getItems()) {
            ProductoEntity producto = item.getProducto();

            PedidoDetalleEntity detalle = new PedidoDetalleEntity();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setVariacion(item.getVariacion());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(producto.getPrecio());

            detalleRepository.save(detalle);

            if (item.getVariacion() != null) {
                item.getVariacion().setStock(item.getVariacion().getStock() - item.getCantidad());
            } else {
                producto.setStock(producto.getStock() - item.getCantidad());
            }
            productoRepository.save(producto);
        }

        carrito.getItems().clear();
        carritoRepository.save(carrito);

        try {
            String correoAdmin = "alextejada025@gmail.com";
            String asunto = "¡Nuevo Pedido Recibido! - #" + pedido.getIdPedidos();
            String mensaje = "Hola Admin,\n\nTienes un nuevo pedido en espera.\n"
                    + "Cliente: " + pedido.getUsuario().getNombre() + "\n"
                    + "Total: $" + pedido.getTotal() + "\n"
                    + "Revisa el panel de control para procesarlo.";

            emailService.enviarNotificacion(correoAdmin, asunto, mensaje);
        } catch (Exception e) {
            System.out.println("Error al enviar notificación al admin: " + e.getMessage());
        }

        return pedido;
    }

    @Override
    public List<PedidoResponse> obtenerPedidoUsuario() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return pedidoRepository.findByUsuario(usuario)
                .stream()
                .filter(p -> p.getOcultoCliente() == null || !p.getOcultoCliente())
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public PedidoResponse pagarPedido(Long id) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        var auth = SecurityContextHolder.getContext().getAuthentication();

        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no existe"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !pedido.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No puedes pagar este pedido");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("El pedido no está pendiente");
        }

        pedido.setEstado(EstadoPedido.PAGADO);
        pedidoRepository.save(pedido);

        return mapToResponse(pedido);
    }

    @Override
    public PedidoResponse cancelarPedido(Long id) {

        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no existe"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("Solo pedidos PENDIENTES pueden cancelarse");
        }

        for (PedidoDetalleEntity item : pedido.getItems()) {
            ProductoEntity producto = item.getProducto();

            if (item.getVariacion() != null) {
                item.getVariacion().setStock(item.getVariacion().getStock() + item.getCantidad());
            } else {
                producto.setStock(producto.getStock() + item.getCantidad());
            }
            productoRepository.save(producto);
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);

        return mapToResponse(pedido);
    }

    @Override
    public List<PedidoResponse> obtenerTodos() {
        return pedidoRepository.findAll()
                .stream()
                .filter(p -> p.getOcultoAdmin() == null || !p.getOcultoAdmin())
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void ocultarPedidoParaAdmin(Long id) {
        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setOcultoAdmin(true);
        pedidoRepository.save(pedido);
    }

    @Override
    public PedidoResponse enviarPedido(Long id) {
        PedidoEntity pedido = pedidoRepository.findById(id).orElseThrow();
        pedido.setEstado(EstadoPedido.ENVIADO);
        pedidoRepository.save(pedido);

        String mensaje = "Hola " + pedido.getUsuario().getNombre() + ",\n\n"
                + "¡Excelentes noticias! Tu compra ya ha sido despachada y va en camino.\n"
                + "Pronto llegará a la dirección registrada: " + pedido.getDireccion() + "\n\n"
                + "¡Gracias por confiar en nosotros!";

        emailService.enviarNotificacion(pedido.getUsuario().getEmail(), "¡Tu compra va en camino! 🚚", mensaje);

        return mapToResponse(pedido);
    }

    @Override
    public PedidoResponse entregarPedido(Long id) {
        PedidoEntity pedido = pedidoRepository.findById(id).orElseThrow();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedidoRepository.save(pedido);

        String mensaje = "Hola " + pedido.getUsuario().getNombre() + ",\n\n"
                + "Confirmamos que tu entrega se ha realizado con éxito.\n"
                + "Esperamos que disfrutes mucho tu producto.\n\n"
                + "¡Vuelve pronto!";

        emailService.enviarNotificacion(pedido.getUsuario().getEmail(), "¡Entrega Confirmada! ✅", mensaje);

        return mapToResponse(pedido);
    }

    private PedidoResponse mapToResponse(PedidoEntity pedido) {
        PedidoResponse dto = new PedidoResponse();

        dto.setIdPedido(pedido.getIdPedidos());
        dto.setTotal(pedido.getTotal() != null ? pedido.getTotal() : 0.0);
        dto.setEstado(pedido.getEstado().name());
        dto.setFecha(pedido.getFecha());

        dto.setUsuarioEmail(pedido.getUsuario().getEmail());
        dto.setNombreCliente(pedido.getUsuario().getNombre());
        dto.setTelefonoCliente(pedido.getUsuario().getTelefono());

        dto.setMetodoEntrega(pedido.getMetodoEntrega());
        dto.setDireccion(pedido.getDireccion());
        dto.setCostoEnvio(pedido.getCostoEnvio() != null ? pedido.getCostoEnvio() : 0.0);

        List<ProductoResumenDTO> resumen = pedido.getItems().stream().map(item -> {
            ProductoResumenDTO i = new ProductoResumenDTO();
            i.setNombre(item.getProducto().getNombre());
            i.setCantidad(item.getCantidad());

            i.setPrecioUnitario(item.getPrecio());
            i.setSubtotal(item.getPrecio() * item.getCantidad());

            if (item.getVariacion() != null) {
                i.setColor(item.getVariacion().getColor() != null ? item.getVariacion().getColor() : "Único");
                i.setTalla(item.getVariacion().getTalla() != null ? item.getVariacion().getTalla() : "Única");
            } else {
                i.setColor("Único");
                i.setTalla("Única");
            }

            if (item.getProducto().getImagenesUrls() != null && !item.getProducto().getImagenesUrls().isEmpty()) {
                i.setImagenUrl(item.getProducto().getImagenesUrls().get(0));
            }
            return i;
        }).toList();

        dto.setProductos(resumen);

        return dto;
    }


    @Override
    public PedidoDetallePageResponse obtenerDetallePedido(Long id) {
        UsuarioEntity usuario = authService.getUsuarioAutenticado();
        var auth = SecurityContextHolder.getContext().getAuthentication();

        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !pedido.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("No autorizado");
        }

        PedidoDetallePageResponse dto = new PedidoDetallePageResponse();
        dto.setIdPedido(pedido.getIdPedidos());
        dto.setFecha(pedido.getFecha());
        dto.setEstado(pedido.getEstado().name());
        dto.setTotal(pedido.getTotal());
        dto.setCostoEnvio(pedido.getCostoEnvio());
        dto.setMetodoEntrega(pedido.getMetodoEntrega());
        dto.setDireccion(pedido.getDireccion());

        List<PedidoDetalleResponse> detalles = pedido.getItems().stream().map(item -> {
            PedidoDetalleResponse d = new PedidoDetalleResponse();

            // 👇 LÓGICA INTELIGENTE PARA MOSTRAR COLOR Y TALLA EN EL RECIBO 👇
            if (item.getVariacion() != null && (!"Único".equals(item.getVariacion().getColor()) || !"Única".equals(item.getVariacion().getTalla()))) {
                String color = item.getVariacion().getColor() != null ? item.getVariacion().getColor() : "Único";
                String talla = item.getVariacion().getTalla() != null ? item.getVariacion().getTalla() : "Única";

                String variacionStr = "";
                if (!"Único".equals(color) && !"Única".equals(talla)) {
                    variacionStr = color + " - " + talla;
                } else if (!"Único".equals(color)) {
                    variacionStr = color;
                } else {
                    variacionStr = talla;
                }

                d.setProducto(item.getProducto().getNombre() + " (" + variacionStr + ")");
            } else {
                d.setProducto(item.getProducto().getNombre());
            }

            d.setPrecio(item.getPrecio());
            d.setCantidad(item.getCantidad());
            d.setSubtotal(item.getPrecio() * item.getCantidad());
            return d;
        }).toList();

        dto.setItems(detalles);
        return dto;
    }

    @Override
    public List<PedidoDetalleEntity> obtenerDetalles(Long id) {
        PedidoEntity pedido = pedidoRepository.findById(id).orElse(null);

        if (pedido != null) {
            return pedido.getItems();
        }
        return null;
    }

    @Override
    public void ocultarPedidoParaCliente(Long id) {
        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            pedido.setOcultoCliente(true);
            pedidoRepository.save(pedido);
        } else {
            throw new RuntimeException("No puedes ocultar un pedido que aún está en proceso");
        }
    }

    @Override
    public void eliminarPedidoDefinitivo(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("El pedido no existe");
        }
        pedidoRepository.deleteById(id);
    }

    @Override
    public Long contarPedidosPendientes() {
        return pedidoRepository.countByEstado(EstadoPedido.PENDIENTE);
    }
}