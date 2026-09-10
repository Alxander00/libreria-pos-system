package com.libreria.pos.controller;

import com.libreria.pos.dto.ClienteFrecuenteDTO;
import com.libreria.pos.dto.IngresosPorCategoriaDTO;
import com.libreria.pos.dto.ProductoMasVendidoDTO;
import com.libreria.pos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/reportes")
public class ReportesAvanzadosController {

    @Autowired
    private PedidoRepository pedidoRepository;

    // 1. Productos más vendidos (por cantidad y por monto)
    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<?> getProductosMasVendidos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "cantidad") String orden) {

        List<ProductoMasVendidoDTO> resultados;
        if ("monto".equalsIgnoreCase(orden)) {
            resultados = pedidoRepository.findProductosMasVendidosPorMonto(inicio, fin);
        } else {
            resultados = pedidoRepository.findProductosMasVendidosPorCantidad(inicio, fin);
        }

        // Limitar a 10 resultados
        if (resultados.size() > 10) {
            resultados = resultados.subList(0, 10);
        }

        return ResponseEntity.ok(resultados);
    }

    // 2. Clientes frecuentes
    @GetMapping("/clientes-frecuentes")
    public ResponseEntity<List<ClienteFrecuenteDTO>> getClientesFrecuentes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<ClienteFrecuenteDTO> resultados = pedidoRepository.findClientesFrecuentes(inicio, fin);
        // Limitar a 10
        if (resultados.size() > 10) {
            resultados = resultados.subList(0, 10);
        }
        return ResponseEntity.ok(resultados);
    }

    // 3. Ingresos por categoría
    @GetMapping("/ingresos-por-categoria")
    public ResponseEntity<List<IngresosPorCategoriaDTO>> getIngresosPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<IngresosPorCategoriaDTO> resultados = pedidoRepository.findIngresosPorCategoria(inicio, fin);
        return ResponseEntity.ok(resultados);
    }
}