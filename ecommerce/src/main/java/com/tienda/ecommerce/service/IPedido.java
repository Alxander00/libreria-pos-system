package com.tienda.ecommerce.service;

import com.tienda.ecommerce.dto.PedidoDetallePageResponse;
import com.tienda.ecommerce.dto.PedidoRequest;
import com.tienda.ecommerce.dto.PedidoResponse;
import com.tienda.ecommerce.entities.PedidoDetalleEntity;
import com.tienda.ecommerce.entities.PedidoEntity;
import com.tienda.ecommerce.entities.UsuarioEntity;

import java.util.List;

public interface IPedido {
    PedidoEntity crearPedido(PedidoRequest request);
    List<PedidoResponse> obtenerPedidoUsuario();
    PedidoResponse pagarPedido(Long id);
    PedidoResponse cancelarPedido(Long id);

    //ADMIN
    List<PedidoResponse> obtenerTodos();
    PedidoResponse enviarPedido(Long id);
    PedidoResponse entregarPedido(Long id);

    PedidoDetallePageResponse obtenerDetallePedido(Long id);
    List<PedidoDetalleEntity> obtenerDetalles(Long id);

    void ocultarPedidoParaCliente(Long id);
    void eliminarPedidoDefinitivo(Long id);

    void ocultarPedidoParaAdmin(Long id);

    Long contarPedidosPendientes();
}
