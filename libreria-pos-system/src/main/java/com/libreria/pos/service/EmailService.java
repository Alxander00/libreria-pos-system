package com.libreria.pos.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Método para correos estándar (Texto plano) por si lo usas en otra parte
    public void enviarNotificacion(String to, String subject, String body) {
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom("alextejada025@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Error al enviar correo: " + e.getMessage());
        }
    }

    //  NUEVO MÉTODO PARA ENVIAR CORREOS CON DISEÑO HTML PROFESIONAL
    public void enviarNotificacionHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true indica que soporta contenido multipart (necesario para HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("alextejada025@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // El 'true' activa el formato HTML

            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Error al enviar correo HTML: " + e.getMessage());
        }
    }


    public void enviarFacturaConAdjuntos(String to, String subject, String htmlBody, byte[] pdfBytes, String jsonContent, String idPedido) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("alextejada025@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            // 1. Adjuntar el PDF
            helper.addAttachment("DTE_Factura_" + idPedido + ".pdf", new ByteArrayResource(pdfBytes));

            // 2. Adjuntar el JSON (Documento Tributario original)
            helper.addAttachment("DTE_" + idPedido + ".json", new ByteArrayResource(jsonContent.getBytes("UTF-8")));

            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Error al enviar correo con adjuntos: " + e.getMessage());
        }
    }
}