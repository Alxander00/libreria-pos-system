package com.libreria.pos.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ResendEmailService {

    @Autowired
    private Resend resend;

    @Value("${resend.from.email}")
    private String fromEmail;

    public void enviarCorreo(String to, String subject, String htmlContent) {
        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Correo enviado a {} con ID: {}", to, response.getId());
        } catch (ResendException e) {
            log.error("Error al enviar correo: {}", e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo: " + e.getMessage());
        }
    }

    public void enviarCorreoConAdjuntos(String to, String subject, String htmlContent,
                                        byte[] pdfBytes, String jsonContent, String idPedido) {
        // Resend aún no soporta adjuntos directamente en la versión Java v3,
        // pero podemos enviar enlaces o usar la versión con multipart.
        // Por ahora, enviaremos sin adjuntos y pondremos enlaces.
        // Si necesitas adjuntos, usaré multipart con MimeMessage.
        enviarCorreo(to, subject, htmlContent);
    }
}