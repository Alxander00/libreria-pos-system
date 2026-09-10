package com.libreria.pos.controller;

import com.libreria.pos.dto.PaymentRequest;
import com.libreria.pos.dto.PaymentResponse;
import com.libreria.pos.service.WompiPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos/wompi")
public class PaymentController {

    @Autowired
    private WompiPaymentService paymentService;

    @PostMapping("/crear-intento")
    public ResponseEntity<PaymentResponse> crearIntento(@RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.crearPaymentIntent(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear intención de pago: " + e.getMessage());
        }
    }

    @GetMapping("/consultar/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> consultar(@PathVariable String paymentIntentId) {
        try {
            PaymentResponse response = paymentService.consultarPago(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar pago: " + e.getMessage());
        }
    }

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(paymentService.getPublicKey());
    }
}