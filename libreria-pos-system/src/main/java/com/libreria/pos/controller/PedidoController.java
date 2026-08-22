package com.libreria.pos.controller;

import com.libreria.pos.dto.PedidoDetallePageResponse;
import com.libreria.pos.dto.PedidoRequest;
import com.libreria.pos.dto.PedidoResponse;
import com.libreria.pos.dto.PosPedidoRequest;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.service.IPedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private IPedido pedidoService;

    // CREAR PEDIDO DESDE CARRITO
    @PostMapping("/crear")
    public ResponseEntity<PedidoEntity> crearPedido(@RequestBody PedidoRequest request) {
        return ResponseEntity.ok(pedidoService.crearPedido(request));
    }

    // HISTORIAL DE PEDIDOS DEL USUARIO
    @GetMapping("/mis-pedidos")
    public List<PedidoResponse> misPedidos() {
        return pedidoService.obtenerPedidoUsuario();
    }

    // PAGAR PEDIDO
    @PostMapping("/{id}/pagar")
    public PedidoResponse pagarPedido(@PathVariable Long id) {
        return pedidoService.pagarPedido(id);
    }

    // CANCELAR PEDIDO
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cancelarPedido(id));
    }

    // DETALLE DE UN PEDIDO (DEL USUARIO LOGUEADO)
    @GetMapping("/{id}")
    public PedidoDetallePageResponse detallePedido(@PathVariable Long id) {
        return pedidoService.obtenerDetallePedido(id);
    }

    // ==========================================
    // RUTAS EXCLUSIVAS PARA EL ADMINISTRADOR
    // ==========================================

    // VER TODOS LOS PEDIDOS DE LA TIENDA
    @GetMapping("/todos")
    public List<PedidoResponse> obtenerTodosLosPedidos() {
        return pedidoService.obtenerTodos();
    }

    // EL ADMIN CONFIRMA QUE YA RECIBIÓ LA TRANSFERENCIA
    @PutMapping("/{id}/confirmar-pago")
    public ResponseEntity<PedidoResponse> adminConfirmarPago(@PathVariable Long id) {
        // Reutilizamos tu lógica de pagar
        return ResponseEntity.ok(pedidoService.pagarPedido(id));
    }

    // EL ADMIN MARCA QUE EL PAQUETE YA VA EN CAMINO
    @PutMapping("/{id}/enviar")
    public ResponseEntity<PedidoResponse> adminEnviarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.enviarPedido(id));
    }

    // EL ADMIN MARCA QUE EL CLIENTE YA RECIBIÓ SU COMPRA
    @PutMapping("/{id}/entregar")
    public ResponseEntity<PedidoResponse> adminEntregarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.entregarPedido(id));
    }

    // Endpoint para el CLIENTE (Ocultar)
    @PutMapping("/{id}/ocultar")
    public ResponseEntity<?> ocultarPedido(@PathVariable Long id) {
        pedidoService.ocultarPedidoParaCliente(id);
        return ResponseEntity.ok().build();
    }

    // Endpoint para el ADMIN (Borrar de la DB)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedidoDefinitivo(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/ocultar-admin")
    public ResponseEntity<?> ocultarAdmin(@PathVariable Long id) {
        pedidoService.ocultarPedidoParaAdmin(id);
        return ResponseEntity.ok().build();
    }

    // Endpoint para el globo de notificaciones del Admin
    @GetMapping("/admin/pendientes/count")
    public ResponseEntity<Long> contarPedidosPendientes() {
        return ResponseEntity.ok(pedidoService.contarPedidosPendientes());
    }

    // ==========================================
    // ENDPOINT EXCLUSIVO PARA PUNTO DE VENTA (POS)
    // ==========================================
    @PostMapping("/pos/crear")
    public ResponseEntity<PedidoResponse> crearPedidoPos(@RequestBody PosPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.crearPedidoPos(request));
    }
}
