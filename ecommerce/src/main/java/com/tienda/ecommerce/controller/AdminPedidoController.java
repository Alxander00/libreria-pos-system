package com.tienda.ecommerce.controller;

import com.tienda.ecommerce.dto.PedidoResponse;
import com.tienda.ecommerce.entities.PedidoDetalleEntity;
import com.tienda.ecommerce.repository.PedidoRepository;
import com.tienda.ecommerce.service.IPedido;
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
