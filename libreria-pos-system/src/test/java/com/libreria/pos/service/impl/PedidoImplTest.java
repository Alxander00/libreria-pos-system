package com.libreria.pos.service.impl;

import com.libreria.pos.dto.PedidoRequest;
import com.libreria.pos.entities.CarritoEntity;
import com.libreria.pos.entities.EstadoPedido;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.repository.*;
import com.libreria.pos.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoImplTest {

    // Mocks de los repositorios
    @Mock private PedidoRepository pedidoRepository;
    @Mock private PedidoDetalleRepository detalleRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private CarritoRepository carritoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    // Mocks de los servicios
    @Mock private ICarrito carritoService;
    @Mock private AuthService authService;
    @Mock private EmailService emailService;
    @Mock private com.libreria.pos.service.DteBuilderService dteBuilderService;
    @Mock private com.libreria.pos.service.HaciendaService haciendaService;
    @Mock private com.libreria.pos.service.QrService qrService;
    @Mock private PdfService pdfService;

    @InjectMocks
    private PedidoImpl pedidoService;

    @Test
    void testCrearPedido_CarritoVacio_DeberiaLanzarExcepcion() {
        // Configuramos el mock del carrito para que devuelva una lista vacía
        CarritoEntity carrito = new CarritoEntity();
        carrito.setItems(new ArrayList<>());
        when(carritoService.obtenerCarritoUsuario()).thenReturn(carrito);

        // Intentamos crear un pedido y esperamos que explote con "El carrito está vacío"
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pedidoService.crearPedido(new PedidoRequest());
        });

        assertEquals("El carrito está vacío", exception.getMessage());
    }

    @Test
    void testCancelarPedido_PedidoNoExistente_DeberiaLanzarExcepcion() {
        // Configuramos el repositorio para que no encuentre nada
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // Esperamos que lance RuntimeException
        assertThrows(RuntimeException.class, () -> {
            pedidoService.cancelarPedido(999L);
        });
    }
}