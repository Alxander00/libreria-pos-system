package com.libreria.pos.controller;

import com.libreria.pos.dto.PagoResponse;
import com.libreria.pos.service.IPago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private IPago pagoService;

    @PostMapping("/pagar/{pedidoId}")
    public PagoResponse pagar(@PathVariable Long pedidoId, @RequestParam String metodo){
        return pagoService.pagarPedido(pedidoId, metodo);
    }


}
