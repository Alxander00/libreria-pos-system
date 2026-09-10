package com.libreria.pos.controller;

import com.libreria.pos.dto.DescuentoRequest;
import com.libreria.pos.service.IProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/descuentos")
public class DescuentoController {

    @Autowired
    private IProducto productoService;

    @PostMapping("/aplicar")
    public ResponseEntity<Map<String, Object>> aplicarDescuento(@RequestBody DescuentoRequest request) {
        // El GlobalExceptionHandler capturará cualquier excepción
        productoService.aplicarDescuentoPro(request);

        Map<String, Object> response = new HashMap<>();
        response.put("exito", true);
        response.put("mensaje", "Descuento del " + request.getValor() + "% aplicado exitosamente.");
        return ResponseEntity.ok(response);
    }
}