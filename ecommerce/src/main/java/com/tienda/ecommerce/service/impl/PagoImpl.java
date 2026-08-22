package com.tienda.ecommerce.service.impl;

import com.tienda.ecommerce.dto.PagoResponse;
import com.tienda.ecommerce.entities.*;
import com.tienda.ecommerce.repository.PagoRepository;
import com.tienda.ecommerce.repository.PedidoRepository;
import com.tienda.ecommerce.service.IPago;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PagoImpl implements IPago {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    @Transactional
    public PagoResponse pagarPedido(Long pedidoId, String metodo) {

        PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no existe"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("El pedido ya fue pagado o no es válido");
        }

        PagoEntity pago = new PagoEntity();
        pago.setPedido(pedido);
        pago.setMetodoPago(MetodoPago.valueOf(metodo.toUpperCase()));
        pago.setMonto(pedido.getTotal());
        pago.setEstadoPago(EstadoPago.COMPLETADO);
        pago.setFecha(LocalDateTime.now());

        pagoRepository.save(pago);

        pedido.setEstado(EstadoPedido.PAGADO);
        pedidoRepository.save(pedido);

        PagoResponse response = new PagoResponse();
        response.setIdPago(pago.getIdPago());
        response.setIdPedido(pedido.getIdPedidos());
        response.setMetodoPago(pago.getMetodoPago().name());
        response.setEstado(pago.getEstadoPago().name());
        response.setMonto(pago.getMonto());
        response.setFecha(pago.getFecha());

        return response;
    }
}
