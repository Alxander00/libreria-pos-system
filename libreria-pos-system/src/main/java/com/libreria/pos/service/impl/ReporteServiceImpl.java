package com.libreria.pos.service.impl;

import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.repository.PedidoRepository;
import com.libreria.pos.service.IReporteService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteServiceImpl implements IReporteService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public ByteArrayInputStream generarExcelVentas(LocalDateTime desde, LocalDateTime hasta) {
        List<PedidoEntity> pedidos;
        if (desde != null && hasta != null) {
            pedidos = pedidoRepository.findAll().stream()
                    .filter(p -> p.getFecha().isAfter(desde) && p.getFecha().isBefore(hasta))
                    .filter(p -> p.getEstado().name().equals("ENTREGADO") || p.getEstado().name().equals("PAGADO"))
                    .toList();
        } else {
            pedidos = pedidoRepository.findAll().stream()
                    .filter(p -> p.getEstado().name().equals("ENTREGADO") || p.getEstado().name().equals("PAGADO"))
                    .toList();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ventas");
            // Cabeceras
            String[] headers = {"ID Pedido", "Fecha", "Cliente", "Total", "Método de Pago", "Estado"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(crearEstiloCabecera(workbook));
            }
            // Datos
            int rowNum = 1;
            for (PedidoEntity p : pedidos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getIdPedidos());
                row.createCell(1).setCellValue(p.getFecha().toString());
                row.createCell(2).setCellValue(p.getUsuario().getNombre());
                row.createCell(3).setCellValue(p.getTotal());
                row.createCell(4).setCellValue(p.getMetodoPago() != null ? p.getMetodoPago() : "N/A");
                row.createCell(5).setCellValue(p.getEstado().name());
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel", e);
        }
    }

    private CellStyle crearEstiloCabecera(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}