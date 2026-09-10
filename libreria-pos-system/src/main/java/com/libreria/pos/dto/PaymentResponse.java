package com.libreria.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String clientSecret; // Para confirmar el pago en el frontend
    private String paymentIntentId;
    private String status;
    private String message;
}