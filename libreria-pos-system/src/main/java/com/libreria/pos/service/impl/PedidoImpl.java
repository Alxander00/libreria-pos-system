package com.libreria.pos.service.impl;

import com.libreria.pos.dto.*;
import com.libreria.pos.entities.*;
import com.libreria.pos.repository.*;
import com.libreria.pos.dto.*;
import com.libreria.pos.entities.*;
import com.libreria.pos.repository.*;
import com.libreria.pos.service.*;
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

    @Autowired
    private com.libreria.pos.service.DteBuilderService dteBuilderService;

    @Autowired
    private com.libreria.pos.service.HaciendaService haciendaService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Autowired
    private com.libreria.pos.service.QrService qrService;

    @Autowired
    private PdfService pdfService;

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
    @Transactional
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

        // 1. Cambiamos el estado a PAGADO
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido = pedidoRepository.save(pedido);

        // 2. Disparamos la Facturación Electrónica al Simulador de Hacienda
        procesarFacturacionElectronica(pedido);

        // 3. Volvemos a guardar para asegurar que el Sello de Recepción se guarde en MySQL
        pedidoRepository.save(pedido);

        // 4. Enviar correo usando nuestra función blindada auxiliar (que está al final del archivo)
        enviarCorreoFacturaCliente(pedido);

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

        // =========================================================================
        // 🧾 GENERACIÓN DEL CÓDIGO QR FISCAL (MINISTERIO DE HACIENDA) 🧾
        // =========================================================================
        if (pedido.getCodigoGeneracion() != null) {
            // "00" indica ambiente de pruebas. Cuando pases a producción, solo cambia a "01"
            String qrB64 = qrService.generarQrBase64ParaPedido(pedido, "00");
            dto.setCodigoQr(qrB64);
        }

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

    @Override
    @Transactional
    public PedidoResponse crearPedidoPos(PosPedidoRequest request) {
        // 1. Obtener el usuario (cajero/admin) logueado
        UsuarioEntity cajero = authService.getUsuarioAutenticado();

        // 2. Crear el pedido
        PedidoEntity pedido = new PedidoEntity();
        pedido.setUsuario(cajero);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setMetodoEntrega("RETIRO");
        pedido.setDireccion("Venta en Mostrador - " + cajero.getNombre());
        pedido.setCostoEnvio(0.0);
        pedido.setMetodoPago(request.getMetodoPago());

        double total = 0.0;

        // 3. Procesar cada producto
        for (PosPedidoRequest.ItemPos itemReq : request.getItems()) {
            ProductoEntity producto = productoRepository.findById(itemReq.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.getIdProducto()));

            ProductoVariacionEntity variacion = null;
            Long stockDisponible = 0L;

            if (itemReq.getIdVariacion() != null) {
                variacion = producto.getVariaciones().stream()
                        .filter(v -> v.getIdVariacion().equals(itemReq.getIdVariacion()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Variación no encontrada"));
                stockDisponible = variacion.getStock();
            } else {
                if (producto.getVariaciones().isEmpty()) {
                    throw new RuntimeException("El producto no tiene stock configurado");
                }
                variacion = producto.getVariaciones().get(0);
                stockDisponible = variacion.getStock();
            }

            if (stockDisponible < itemReq.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + producto.getNombre() +
                        ". Disponible: " + stockDisponible);
            }

            variacion.setStock(stockDisponible - itemReq.getCantidad());

            double precioUnitario = producto.getPrecio();
            if (producto.getDescuento() > 0) {
                precioUnitario = precioUnitario - (precioUnitario * (producto.getDescuento() / 100.0));
            }

            PedidoDetalleEntity detalle = new PedidoDetalleEntity();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setVariacion(variacion);
            detalle.setCantidad(itemReq.getCantidad());
            detalle.setPrecio(precioUnitario);

            pedido.getItems().add(detalle);

            total += precioUnitario * itemReq.getCantidad();
        }

        pedido.setTotal(total);

        // 1. Guardamos primero para que MySQL le asigne el IDPedidos oficial
        pedido = pedidoRepository.save(pedido);

        // 2. Ejecutamos el simulador de Hacienda (Esto imprimirá el JSON en tu consola)
        procesarFacturacionElectronica(pedido);

        // 3. Volvemos a guardar para que el código de generación, número de control y sello se queden en la BD
        pedidoRepository.save(pedido);

        // 👇 4. ENVIAR CORREO CON LA FUNCIÓN BLINDADA 👇
        enviarCorreoFacturaCliente(pedido);

        return mapToResponse(pedido);
    }

    private void procesarFacturacionElectronica(PedidoEntity pedido) {
        try {
            // 1. Construir la factura con el molde
            com.libreria.pos.dto.dte.FacturaElectronicaDTO facturaDte = dteBuilderService.construirFactura(pedido);

            // 2. Enviar al simulador de Hacienda
            String respuestaHaciendaJson = haciendaService.firmarFactura(facturaDte);

            // 3. Extraer el Sello de Recepción
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(respuestaHaciendaJson);
            String estado = jsonNode.get("estado").asText();

            if ("PROCESADO".equals(estado)) {
                String sello = jsonNode.get("selloRecibido").asText();

                // Asignamos los datos fiscales a la entidad
                pedido.setCodigoGeneracion(facturaDte.getIdentificacion().getCodigoGeneracion());
                pedido.setNumeroControl(facturaDte.getIdentificacion().getNumeroControl());
                pedido.setSelloRecibido(sello);
            } else {
                System.err.println("Hacienda rechazó la factura: " + respuestaHaciendaJson);
            }
        } catch (Exception e) {
            System.err.println("Error en facturación electrónica: " + e.getMessage());
        }
    }

    // =========================================================================
    // MÉTODO AUXILIAR BLINDADO PARA ENVIAR CORREOS DE FACTURACIÓN
    // =========================================================================
    private void enviarCorreoFacturaCliente(PedidoEntity pedido) {
        try {
            if (pedido.getUsuario() == null || pedido.getUsuario().getEmail() == null) {
                System.err.println("El cliente no tiene correo registrado. Omitiendo envío.");
                return;
            }

            String correoCliente = pedido.getUsuario().getEmail();
            String linkTicket = "http://127.0.0.1:5500/ticket.html?id=" + pedido.getIdPedidos();
            boolean esRetiro = "RETIRO".equalsIgnoreCase(pedido.getMetodoEntrega());

            String asunto = esRetiro
                    ? "Notificación de Entrega y Facturación Electrónica | MI LIBRERÍA"
                    : "Confirmación de Pago y Facturación Electrónica | MI LIBRERÍA";

            String descripcionTexto = esRetiro
                    ? "Le confirmamos que su pedido ha sido entregado exitosamente en nuestra sucursal. Conforme a la normativa del Ministerio de Hacienda, hemos emitido su <b>Factura Electrónica (DTE)</b> correspondiente."
                    : "Le informamos que su pago ha sido procesado con éxito y su pedido se encuentra en fase de despacho. Conforme a la normativa del Ministerio de Hacienda, hemos emitido su <b>Factura Electrónica (DTE)</b>.";

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaActual = java.time.LocalDateTime.now().format(formatter);

            String nombreCliente = pedido.getUsuario().getNombre() != null ? pedido.getUsuario().getNombre().toUpperCase() : "CLIENTE";

            String mensajeHtml = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 0; border: 1px solid #e0e0e0; border-radius: 8px; background-color: #ffffff; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);\">"
                    + "<div style=\"background-color: #000000; color: #ffffff; padding: 35px 20px; text-align: center;\">"
                    + "<h1 style=\"margin: 0; font-size: 24px; letter-spacing: 2px; font-weight: 600;\">MI LIBRERÍA</h1>"
                    + "<p style=\"margin: 8px 0 0; font-size: 11px; color: #b3b3b3; text-transform: uppercase; letter-spacing: 3px;\">Comprobante Fiscal Electrónico</p>"
                    + "</div>"
                    + "<div style=\"padding: 40px 30px;\">"
                    + "<p style=\"font-size: 16px; color: #2c3e50; margin-top: 0; font-weight: 500;\">Estimado(a) <b>" + nombreCliente + "</b>,</p>"
                    + "<p style=\"font-size: 14px; color: #555555; line-height: 1.6; margin-bottom: 30px;\">" + descripcionTexto + "</p>"
                    + "<div style=\"background-color: #f8f9fa; border: 1px solid #eaedf1; border-left: 4px solid #198754; padding: 20px; border-radius: 6px; margin: 25px 0;\">"
                    + "<table style=\"width: 100%; font-size: 14px; color: #444; border-collapse: collapse;\">"
                    + "<tr><td style=\"padding-bottom: 10px; color: #7f8c8d;\"><strong>Fecha de Emisión:</strong></td><td style=\"text-align: right; padding-bottom: 10px;\">" + fechaActual + "</td></tr>"
                    + "<tr><td style=\"padding-bottom: 10px; color: #7f8c8d;\"><strong>Modalidad de Entrega:</strong></td><td style=\"text-align: right; padding-bottom: 10px;\">" + (esRetiro ? "Retiro en Sucursal" : "Envío a Domicilio") + "</td></tr>"
                    + "<tr><td style=\"padding-top: 12px; border-top: 1px solid #e0e6ed; color: #2c3e50;\"><strong>TOTAL ABONADO:</strong></td><td style=\"text-align: right; padding-top: 12px; border-top: 1px solid #e0e6ed; color: #198754; font-size: 18px; font-weight: 700;\">$" + String.format("%.2f", pedido.getTotal()) + "</td></tr>"
                    + "</table>"
                    + "</div>"
                    + "<div style=\"text-align: center; margin: 40px 0 30px;\">"
                    + "<a href=\"" + linkTicket + "\" style=\"background-color: #0d6efd; color: #ffffff; padding: 14px 30px; text-decoration: none; border-radius: 4px; font-weight: 600; font-size: 14px; display: inline-block;\">📄 Ver Representación Gráfica (DTE)</a>"
                    + "</div>"
                    + "<p style=\"font-size: 12px; color: #95a5a6; text-align: center; margin-top: 30px; line-height: 1.5;\">Si presenta inconvenientes con el botón, acceda al documento mediante el siguiente enlace:<br><a href=\"" + linkTicket + "\" style=\"color: #3498db; word-break: break-all; text-decoration: none;\">" + linkTicket + "</a></p>"
                    + "</div>"
                    + "<div style=\"background-color: #f4f6f7; padding: 25px; text-align: center; font-size: 11px; color: #7f8c8d; border-top: 1px solid #eaedf1;\">"
                    + "<p style=\"margin: 0 0 8px;\">Documento Tributario Electrónico emitido conforme a las regulaciones del Ministerio de Hacienda de El Salvador.</p>"
                    + "<p style=\"margin: 0 0 8px;\">Este es un mensaje generado automáticamente. Por favor, no responda a este correo.</p>"
                    + "<p style=\"margin: 0; font-weight: 600;\">MI LIBRERÍA © " + java.time.Year.now().getValue() + " | Atiquizaya, Ahuachapán.</p>"
                    + "</div>"
                    + "</div>";

            // Bloque 1: Intentar generar PDF sin que rompa el código
            byte[] pdfBytes = null;
            try {
                pdfBytes = pdfService.generarFacturaPdf(pedido);
            } catch (Exception e) {
                System.err.println("Advertencia: Falló la generación del PDF - " + e.getMessage());
            }

            // Bloque 2: Intentar extraer JSON sin que rompa el código
            String jsonContent = null;
            try {
                com.libreria.pos.dto.dte.FacturaElectronicaDTO facturaDte = dteBuilderService.construirFactura(pedido);
                jsonContent = objectMapper.writeValueAsString(facturaDte);
            } catch (Exception e) {
                System.err.println("Advertencia: Falló la extracción del JSON - " + e.getMessage());
            }

            // Bloque 3: Enviar (Con adjuntos si hay éxito, o solo HTML si hubo un fallo parcial)
            if (pdfBytes != null && pdfBytes.length > 0 && jsonContent != null) {
                emailService.enviarFacturaConAdjuntos(correoCliente, asunto, mensajeHtml, pdfBytes, jsonContent, String.valueOf(pedido.getIdPedidos()));
            } else {
                emailService.enviarNotificacionHtml(correoCliente, asunto, mensajeHtml);
                System.err.println("Correo enviado sin adjuntos por un error en PDF o JSON.");
            }

        } catch (Exception e) {
            System.err.println("Error crítico al enviar el correo: " + e.getMessage());
        }
    }
}