package com.libreria.pos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.pos.config.WompiConfig;
import com.libreria.pos.dto.PaymentRequest;
import com.libreria.pos.dto.PaymentResponse;
import com.libreria.pos.entities.EstadoPedido;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WompiPaymentService {

    @Autowired
    private WompiConfig wompiConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Crear una transacción de pago en Wompi El Salvador
     */
    public PaymentResponse crearPaymentIntent(PaymentRequest request) throws Exception {
        PedidoEntity pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("El pedido no está pendiente de pago");
        }

        // Convertir el monto a centavos (Wompi usa la moneda más pequeña, en USD son centavos)
        long amountInCents = Math.round(pedido.getTotal() * 100);

        // Construir el payload para Wompi
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount_in_cents", amountInCents);
        payload.put("currency", "USD"); // ¡CAMBIADO A USD!
        payload.put("reference", "Pedido #" + pedido.getIdPedidos());
        payload.put("customer_email", pedido.getUsuario().getEmail());

        // Datos del cliente (opcional)
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("email", pedido.getUsuario().getEmail());
        customerData.put("full_name", pedido.getUsuario().getNombre());
        payload.put("customer_data", customerData);

        // IMPORTANTE: En El Salvador, el pago con tarjeta requiere un "token" generado por el JS de Wompi en el frontend.
        // El campo "paymentMethodId" de tu request debe ser ese token.
        Map<String, String> paymentMethod = new HashMap<>();
        paymentMethod.put("type", "CARD");
        paymentMethod.put("token", request.getPaymentMethodId()); // Asegúrate de enviar el token desde el frontend

        payload.put("payment_method", paymentMethod);

        // Redirigir al frontend después del pago
        if (request.getReturnUrl() != null && !request.getReturnUrl().isEmpty()) {
            payload.put("redirect_url", request.getReturnUrl());
        }

        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + wompiConfig.getPrivateKey());
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Enviar solicitud a Wompi (Endpoint correcto para El Salvador: /transactions)
        String url = wompiConfig.getBaseUrl() + "/transactions";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al crear transacción en Wompi: " + response.getBody());
        }

        // Parsear respuesta
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        JsonNode data = jsonResponse.get("data");

        PaymentResponse paymentResponse = new PaymentResponse();
        // Wompi SV no devuelve "client_secret", devuelve el ID de la transacción
        paymentResponse.setPaymentIntentId(data.get("id").asText());
        paymentResponse.setStatus(data.get("status").asText());
        paymentResponse.setMessage("Transacción creada exitosamente");

        return paymentResponse;
    }

    /**
     * Obtener el estado de un pago (para consultar después)
     */
    public PaymentResponse consultarPago(String transactionId) throws Exception {
        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + wompiConfig.getPrivateKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Consultar en Wompi (Endpoint correcto: /transactions/{id})
        String url = wompiConfig.getBaseUrl() + "/transactions/" + transactionId;
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al consultar transacción en Wompi: " + response.getBody());
        }

        // Parsear respuesta
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        JsonNode data = jsonResponse.get("data");

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentIntentId(data.get("id").asText());
        paymentResponse.setStatus(data.get("status").asText());
        paymentResponse.setMessage("Transacción consultada exitosamente");

        return paymentResponse;
    }

    public String getPublicKey() {
        return wompiConfig.getPublicKey();
    }
}