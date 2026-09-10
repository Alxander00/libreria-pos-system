package com.libreria.pos.service.impl;

import com.libreria.pos.dto.PedidoRequest;
import com.libreria.pos.dto.PedidoResponse;
import com.libreria.pos.entities.*;
import com.libreria.pos.repository.*;
import com.libreria.pos.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// 🟢 NUEVA ANOTACIÓN: Le dice a Mockito que sea flexible si no usamos todos los mocks en cada test
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PedidoImplTestAvanzado {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private PedidoDetalleRepository detalleRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private CarritoRepository carritoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ICarrito carritoService;
    @Mock private AuthService authService;
    @Mock private EmailService emailService;
    @Mock private com.libreria.pos.service.DteBuilderService dteBuilderService;
    @Mock private com.libreria.pos.service.HaciendaService haciendaService;
    @Mock private com.libreria.pos.service.QrService qrService;
    @Mock private PdfService pdfService;
    @Mock private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private PedidoImpl pedidoService;

    // Mocks de entidades (necesarios para simular los getters)
    @Mock private CarritoEntity carrito;
    @Mock private CarritoDetalleEntity itemCarrito;

    // Objetos reales (necesarios para usar setters y verificar cambios)
    private UsuarioEntity usuario;
    private ProductoEntity producto;
    private PedidoEntity pedido;

    @BeforeEach
    void setUp() {
        // 1. Configuramos un usuario de prueba
        usuario = new UsuarioEntity();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Cliente Test");
        usuario.setEmail("test@test.com");
        usuario.setTelefono("77778888");

        // 2. Configuramos un producto de prueba
        producto = new ProductoEntity();
        producto.setIdProducto(1L);
        producto.setNombre("Cuaderno");
        producto.setPrecio(2.50);
        producto.setStock(10L);

        // 3. Configuramos el pedido
        pedido = new PedidoEntity();
        pedido.setIdPedidos(1L);
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(5.00);
    }

    // ==========================================
    // TEST 1: Crear pedido con éxito (ESTE YA TE PASABA)
    // ==========================================
    @Test
    void testCrearPedido_Exitoso() {
        // Simulamos el carrito y sus items
        when(carrito.getUsuario()).thenReturn(usuario);
        when(carrito.getItems()).thenReturn(new ArrayList<>(List.of(itemCarrito)));
        when(carrito.getTotal()).thenReturn(5.00);

        when(itemCarrito.getProducto()).thenReturn(producto);
        when(itemCarrito.getCantidad()).thenReturn(2L);
        when(itemCarrito.getVariacion()).thenReturn(null);

        when(carritoService.obtenerCarritoUsuario()).thenReturn(carrito);
        when(pedidoRepository.save(any(PedidoEntity.class))).thenReturn(pedido);

        PedidoRequest request = new PedidoRequest();
        request.setMetodoEntrega("RETIRO");
        request.setDireccion("Retiro en tienda");
        request.setCostoEnvio(0.0);

        PedidoEntity resultado = pedidoService.crearPedido(request);

        assertEquals(EstadoPedido.PENDIENTE, resultado.getEstado());
        assertEquals(5.00, resultado.getTotal());
        assertEquals(8L, producto.getStock());
    }

    // ==========================================
    // TEST 2: Cancelar pedido con éxito (devuelve stock)
    // ==========================================
    @Test
    void testCancelarPedido_Exitoso_DevuelveStock() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoDetalleEntity detalle = new PedidoDetalleEntity();
        detalle.setProducto(producto);
        detalle.setCantidad(2L);
        // 🟢 AQUÍ ESTABA EL ERROR: Faltaba poner el precio
        detalle.setPrecio(2.50);

        pedido.setItems(new ArrayList<>(List.of(detalle)));

        PedidoResponse response = pedidoService.cancelarPedido(1L);

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        assertEquals(12L, producto.getStock()); // 10 original + 2 devueltos
    }

    // ==========================================
    // TEST 3: Pagar pedido con éxito
    // ==========================================
    @Test
    void testPagarPedido_Exitoso() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                )
        );

        when(usuarioRepository.findByEmail("test@test.com")).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenReturn(pedido);

        PedidoResponse response = pedidoService.pagarPedido(1L);

        assertEquals("PAGADO", response.getEstado());
        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());

        SecurityContextHolder.clearContext();
    }
}