package com.libreria.pos.controller;

import com.libreria.pos.dto.PedidoResponse;
import com.libreria.pos.entities.PedidoDetalleEntity;
import com.libreria.pos.service.IPedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/pedidos")
public class AdminPedidoController {

    @Autowired
    private IPedido iPedido;

    @GetMapping
    public List<PedidoResponse> obtenerTodos(){
        return  iPedido.obtenerTodos();
    }

    @GetMapping("/{id}/detalles")
    public List<PedidoDetalleEntity>obtenerDetalles(@PathVariable Long id){
        return iPedido.obtenerDetalles(id);
    }

    @PutMapping("/{id}/enviar")
    public PedidoResponse enviarPedido(@PathVariable Long id){
        return iPedido.enviarPedido(id);
    }

    @PutMapping("/{id}/entregar")
    public PedidoResponse entregarPedido(@PathVariable Long id){
        return iPedido.entregarPedido(id);
    }
}
