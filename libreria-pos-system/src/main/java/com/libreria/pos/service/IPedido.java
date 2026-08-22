package com.libreria.pos.service;

import com.libreria.pos.dto.PedidoDetallePageResponse;
import com.libreria.pos.dto.PedidoRequest;
import com.libreria.pos.dto.PedidoResponse;
import com.libreria.pos.dto.PosPedidoRequest;
import com.libreria.pos.entities.PedidoDetalleEntity;
import com.libreria.pos.entities.PedidoEntity;

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

    PedidoResponse crearPedidoPos(PosPedidoRequest request);
}
