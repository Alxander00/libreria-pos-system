package com.tienda.ecommerce.service;

import com.tienda.ecommerce.dto.PagoResponse;

public interface IPago {

    PagoResponse pagarPedido(Long pedidoId, String metodo);
}
