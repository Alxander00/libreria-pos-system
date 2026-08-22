package com.tienda.ecommerce.controller;

import com.tienda.ecommerce.dto.PagoResponse;
import com.tienda.ecommerce.service.IPago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
