package com.libreria.pos.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.libreria.pos.entities.PedidoEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class QrService {

    public String generarQrBase64ParaPedido(PedidoEntity pedido, String ambiente) {
        try {
            // 1. Armar la URL oficial exigida por el Ministerio de Hacienda
            // Estructura: https://admin.factura.gob.sv/consultaPublica?ambiente=...&codGen=...&fechaEmi=...
            String fechaFormateada = pedido.getFecha().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String urlHacienda = String.format(
                    "https://admin.factura.gob.sv/consultaPublica?ambiente=%s&codGen=%s&fechaEmi=%s",
                    ambiente, // "00" para pruebas, "01" para producción
                    pedido.getCodigoGeneracion(),
                    fechaFormateada
            );

            // 2. Configurar las dimensiones del QR (ej: 250x250 píxeles)
            int width = 250;
            int height = 250;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(urlHacienda, BarcodeFormat.QR_CODE, width, height);

            // 3. Convertir la matriz de bits a una imagen en memoria (PNG)
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            // 4. Codificar los bytes a formato Base64 para enviarlo fácilmente por una API REST
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error al generar el Código QR del DTE: " + e.getMessage());
        }
    }
}