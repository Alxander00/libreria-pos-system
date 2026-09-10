package com.libreria.pos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String RESEND_URL = "https://api.resend.com/emails";

    public void enviarNotificacionHtml(String to, String subject, String htmlBody) {
        enviarCorreo(to, subject, htmlBody, null);
    }

    public void enviarFacturaConAdjuntos(String to, String subject, String htmlBody,
                                         byte[] pdfBytes, String jsonContent, String idPedido) {
        // Preparar adjuntos
        List<Map<String, String>> attachments = new ArrayList<>();
        attachments.add(Map.of(
                "filename", "DTE_Factura_" + idPedido + ".pdf",
                "content", Base64.getEncoder().encodeToString(pdfBytes)
        ));
        attachments.add(Map.of(
                "filename", "DTE_" + idPedido + ".json",
                "content", Base64.getEncoder().encodeToString(jsonContent.getBytes())
        ));
        enviarCorreo(to, subject, htmlBody, attachments);
    }

    private void enviarCorreo(String to, String subject, String htmlBody, List<Map<String, String>> attachments) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("from", fromEmail);
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("html", htmlBody);

            if (attachments != null && !attachments.isEmpty()) {
                payload.put("attachments", attachments);
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Correo enviado con Resend (HTTP). Respuesta: " + response.getBody());
            } else {
                System.err.println("❌ Error al enviar correo (HTTP): " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("❌ Excepción al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}