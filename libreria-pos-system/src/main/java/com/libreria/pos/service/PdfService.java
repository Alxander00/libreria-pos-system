package com.libreria.pos.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.entities.PedidoDetalleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfService {

    @Autowired
    private QrService qrService;

    public byte[] generarFacturaPdf(PedidoEntity pedido) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Márgenes ajustados al estilo DTE formal
            Document document = new Document(PageSize.A4, 30, 30, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Tipografías
            Font fontBoldLg = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);

            // ==========================================
            // 1. ENCABEZADO PRINCIPAL (3 COLUMNAS: QR, EMPRESA, DTE)
            // ==========================================
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{0.9f, 1.4f, 1.1f});

            // Columna 1: Código QR Fiscal (Con limpieza de prefijo Base64)
            PdfPCell cellQr = new PdfPCell();
            cellQr.setBorder(Rectangle.NO_BORDER);
            cellQr.setVerticalAlignment(Element.ALIGN_MIDDLE);
            try {
                String qrB64 = qrService.generarQrBase64ParaPedido(pedido, "00");
                if (qrB64 != null && !qrB64.isEmpty()) {
                    // Limpiamos el prefijo de datos si viene incluido
                    if (qrB64.contains(",")) {
                        qrB64 = qrB64.split(",")[1];
                    }

                    byte[] imageBytes = Base64.getDecoder().decode(qrB64.trim());
                    Image qrImage = Image.getInstance(imageBytes);
                    qrImage.scaleToFit(75, 75);
                    qrImage.setAlignment(Element.ALIGN_CENTER);
                    cellQr.addElement(qrImage);
                }
            } catch (Exception e) {
                System.err.println("No se pudo adjuntar el QR al PDF: " + e.getMessage());
            }
            headerTable.addCell(cellQr);

            // Columna 2: Datos de la Empresa
            PdfPCell cellIzq = new PdfPCell();
            cellIzq.setBorder(Rectangle.NO_BORDER);
            cellIzq.addElement(new Paragraph("MI LIBRERÍA", fontBoldLg));
            cellIzq.addElement(new Paragraph("DTE: FACTURA", fontBold));
            cellIzq.addElement(new Paragraph("Venta al por menor de libros y artículos de papelería", fontNormal));
            cellIzq.addElement(new Paragraph("Atiquizaya, Ahuachapán, El Salvador", fontNormal));
            cellIzq.addElement(new Paragraph("NIT: 0000-000000-000-0", fontNormal));
            cellIzq.addElement(new Paragraph("NRC: 123456-7", fontNormal));
            cellIzq.addElement(new Paragraph("Correo: contacto@milibreria.com", fontNormal));
            headerTable.addCell(cellIzq);

            // Columna 3: Datos de Generación y Sellos Fiscales
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaActual = java.time.LocalDateTime.now().format(formatter);

            PdfPCell cellDer = new PdfPCell();
            cellDer.setBorder(Rectangle.NO_BORDER);
            cellDer.addElement(new Paragraph("Modelo: Previo | Transmisión: Normal", fontSmall));
            cellDer.addElement(new Paragraph("Fecha Generación:", fontBold));
            cellDer.addElement(new Paragraph(fechaActual, fontSmall));
            cellDer.addElement(new Paragraph(" ", fontSmall));

            String codigoGen = pedido.getCodigoGeneracion() != null ? pedido.getCodigoGeneracion() : "PENDIENTE";
            String numControl = pedido.getNumeroControl() != null ? pedido.getNumeroControl() : "PENDIENTE";
            String selloRec = pedido.getSelloRecibido() != null ? pedido.getSelloRecibido() : "Pendiente de Asignación";

            cellDer.addElement(new Paragraph("Código de Generación:", fontBold));
            cellDer.addElement(new Paragraph(codigoGen, fontSmall));
            cellDer.addElement(new Paragraph("Número de Control:", fontBold));
            cellDer.addElement(new Paragraph(numControl, fontSmall));
            cellDer.addElement(new Paragraph("Sello de Recepción:", fontBold));
            cellDer.addElement(new Paragraph(selloRec, fontSmall));
            headerTable.addCell(cellDer);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // ==========================================
            // 2. DATOS DEL CLIENTE (Borde exterior)
            // ==========================================
            PdfPTable clientTable = new PdfPTable(2);
            clientTable.setWidthPercentage(100);
            clientTable.setWidths(new float[]{1f, 1f});

            String nombreCliente = (pedido.getUsuario() != null && pedido.getUsuario().getNombre() != null) ? pedido.getUsuario().getNombre().toUpperCase() : "CLIENTE GENERAL";
            String correoCliente = (pedido.getUsuario() != null && pedido.getUsuario().getEmail() != null) ? pedido.getUsuario().getEmail() : "N/A";
            String telefonoCliente = (pedido.getUsuario() != null && pedido.getUsuario().getTelefono() != null) ? pedido.getUsuario().getTelefono() : "N/A";
            String direccionEnvio = pedido.getDireccion() != null ? pedido.getDireccion() : "Retiro en sucursal";

            PdfPCell cCliente1 = new PdfPCell();
            cCliente1.setBorder(Rectangle.BOX);
            cCliente1.setPadding(5f);
            cCliente1.addElement(new Paragraph("Cliente: " + nombreCliente, fontNormal));
            cCliente1.addElement(new Paragraph("Dirección: " + direccionEnvio, fontNormal));
            cCliente1.addElement(new Paragraph("Condición de Pago: Contado", fontNormal));
            clientTable.addCell(cCliente1);

            PdfPCell cCliente2 = new PdfPCell();
            cCliente2.setBorder(Rectangle.BOX);
            cCliente2.setPadding(5f);
            cCliente2.addElement(new Paragraph("NIT/DUI: 00000000-0", fontNormal));
            cCliente2.addElement(new Paragraph("Correo: " + correoCliente, fontNormal));
            cCliente2.addElement(new Paragraph("Teléfono: " + telefonoCliente, fontNormal));
            clientTable.addCell(cCliente2);

            document.add(clientTable);
            document.add(new Paragraph(" "));

            // ==========================================
            // 3. TABLA DE DETALLE DE PRODUCTOS
            // ==========================================
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 4f, 1.5f, 1.5f, 1.5f, 1.5f});

            String[] headers = {"Cantidad", "Descripción", "Precio Unitario", "Venta No Sujeta", "Venta Exenta", "Venta Gravada"};
            for (String h : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(h, fontBold));
                hCell.setBackgroundColor(new Color(230, 230, 230));
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                hCell.setPadding(4f);
                table.addCell(hCell);
            }

            for (PedidoDetalleEntity item : pedido.getItems()) {
                table.addCell(crearCeldaCentral(String.valueOf(item.getCantidad()), fontNormal));
                table.addCell(crearCeldaIzq(item.getProducto().getNombre(), fontNormal));
                table.addCell(crearCeldaDer(String.format("%.4f", item.getPrecio()), fontNormal));
                table.addCell(crearCeldaDer("0.00", fontNormal));
                table.addCell(crearCeldaDer("0.00", fontNormal));
                table.addCell(crearCeldaDer(String.format("%.2f", item.getPrecio() * item.getCantidad()), fontNormal));
            }
            document.add(table);

            // ==========================================
            // 4. RESUMEN Y TOTALES (2 COLUMNAS INFERIORES)
            // ==========================================
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1.5f, 1f});

            PdfPCell fIzq = new PdfPCell();
            fIzq.setBorder(Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT);
            fIzq.setPadding(8f);
            fIzq.addElement(new Paragraph("Valor en letras:", fontBold));
            fIzq.addElement(new Paragraph("EL MONTO REFLEJADO EN EL TOTAL A PAGAR EN DÓLARES DE LOS ESTADOS UNIDOS DE AMÉRICA.", fontNormal));
            fIzq.addElement(new Paragraph(" ", fontNormal));
            fIzq.addElement(new Paragraph("Observación: Documento emitido por sistema POS/Web.", fontNormal));
            footerTable.addCell(fIzq);

            PdfPTable tTotales = new PdfPTable(2);
            tTotales.setWidthPercentage(100);
            tTotales.setWidths(new float[]{2f, 1f});

            agregarFilaTotal(tTotales, "Suma total de operaciones:", pedido.getTotal(), fontNormal);
            agregarFilaTotal(tTotales, "Monto global desc. (No sujetas):", 0.00, fontNormal);
            agregarFilaTotal(tTotales, "Monto global desc. (Exentas):", 0.00, fontNormal);
            agregarFilaTotal(tTotales, "Monto global desc. (Gravadas):", 0.00, fontNormal);
            agregarFilaTotal(tTotales, "Sub-total:", pedido.getTotal(), fontNormal);
            agregarFilaTotal(tTotales, "IVA Retenido:", 0.00, fontNormal);
            agregarFilaTotal(tTotales, "Retención Renta:", 0.00, fontNormal);
            agregarFilaTotal(tTotales, "Monto total de la operación:", pedido.getTotal(), fontNormal);

            PdfPCell textTotalPagar = new PdfPCell(new Phrase("Total a pagar:", fontBold));
            textTotalPagar.setBorder(Rectangle.NO_BORDER);
            tTotales.addCell(textTotalPagar);

            PdfPCell valorTotalPagar = new PdfPCell(new Phrase("$ " + String.format("%.2f", pedido.getTotal()), fontBold));
            valorTotalPagar.setBorder(Rectangle.NO_BORDER);
            valorTotalPagar.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tTotales.addCell(valorTotalPagar);

            PdfPCell fDer = new PdfPCell(tTotales);
            fDer.setBorder(Rectangle.RIGHT | Rectangle.BOTTOM);
            fDer.setPadding(4f);
            footerTable.addCell(fDer);

            document.add(footerTable);

            // Lema final
            Paragraph lema = new Paragraph("\n\"Impulsando la educación y cultura\"", fontNormal);
            lema.setAlignment(Element.ALIGN_CENTER);
            document.add(lema);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Error al generar el PDF con QR: " + e.getMessage());
            return new byte[0];
        }
    }

    private PdfPCell crearCeldaCentral(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4f);
        return cell;
    }

    private PdfPCell crearCeldaIzq(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4f);
        return cell;
    }

    private PdfPCell crearCeldaDer(String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4f);
        return cell;
    }

    private void agregarFilaTotal(PdfPTable table, String etiqueta, double valor, Font font) {
        PdfPCell cellEti = new PdfPCell(new Phrase(etiqueta, font));
        cellEti.setBorder(Rectangle.NO_BORDER);
        table.addCell(cellEti);

        PdfPCell cellVal = new PdfPCell(new Phrase(String.format("%.2f", valor), font));
        cellVal.setBorder(Rectangle.NO_BORDER);
        cellVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cellVal);
    }
}