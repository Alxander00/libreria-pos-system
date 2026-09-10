package com.libreria.pos.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long pedidoId;
    private String paymentMethodId; // ID del método de pago (token de tarjeta)
    private String returnUrl; // URL de retorno después del pago (opcional para 3D Secure)
}