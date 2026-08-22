package com.tienda.ecommerce.controller;

import com.tienda.ecommerce.dto.DescuentoRequest;
import com.tienda.ecommerce.service.IProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/descuentos")
public class DescuentoController {

    @Autowired
    private IProducto productoService;

    @PostMapping("/aplicar")
    public ResponseEntity<?> aplicarDescuento(@RequestBody DescuentoRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Mandamos todo al servicio Pro
            productoService.aplicarDescuentoPro(request);

            response.put("exito", true);
            response.put("mensaje", "Descuento del " + request.getValor() + "% aplicado exitosamente.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("exito", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("exito", false);
            response.put("error", "Error interno al aplicar el descuento: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}