package com.libreria.pos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${whatsapp.token}")
    private String token;

    @Value("${whatsapp.phone_id}")
    private String phoneId;

    @Value("${whatsapp.base_url}")
    private String baseUrl;

    public void enviarMensaje(String telefono, String mensaje) {
        try {
            // Limpiamos el teléfono (quitamos espacios y guiones) y agregamos el código de país si falta
            String numeroLimpio = telefono.replaceAll("[^0-9]", "");
            if (!numeroLimpio.startsWith("503")) {
                numeroLimpio = "503" + numeroLimpio; // Código de El Salvador
            }

            String url = baseUrl + "/" + phoneId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", numeroLimpio);
            body.put("type", "text");

            Map<String, String> text = new HashMap<>();
            text.put("body", mensaje);
            body.put("text", text);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Enviamos la petición a Meta. Si falla, no rompemos el proceso principal del pedido.
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("WhatsApp enviado correctamente: " + response.getStatusCode());

        } catch (Exception e) {
            // Solo imprimimos el error para no detener el flujo del pedido
            System.err.println("Error al enviar WhatsApp (el pedido continúa): " + e.getMessage());
        }
    }
}