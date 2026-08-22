package com.libreria.pos.service;

import com.libreria.pos.dto.PagoResponse;

public interface IPago {

    PagoResponse pagarPedido(Long pedidoId, String metodo);
}
