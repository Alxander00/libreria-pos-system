package com.libreria.pos.controller;

import com.libreria.pos.entities.EstadoPedido;
import com.libreria.pos.entities.ProductoEntity;
import com.libreria.pos.repository.CategoriaRepository;
import com.libreria.pos.repository.PedidoRepository;
import com.libreria.pos.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository; // ✅ Inyectado

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        // Consultas optimizadas
        double ingHoy = pedidoRepository.sumarIngresosEntreFechas(hoy.atStartOfDay(), hoy.atTime(23,59,59));
        double ingSemana = pedidoRepository.sumarIngresosEntreFechas(inicioSemana.atStartOfDay(), LocalDateTime.now());
        double ingMes = pedidoRepository.sumarIngresosEntreFechas(inicioMes.atStartOfDay(), LocalDateTime.now());

        long pendientes = pedidoRepository.countByEstado(EstadoPedido.PENDIENTE);
        long totalProductos = productoRepository.count();
        long totalCategorias = categoriaRepository.count();

        // Ventas por día (últimos 7 días)
        List<Map<String, Object>> ventasSemanales = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E d", new Locale("es", "ES"));

        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            LocalDateTime inicioDia = fecha.atStartOfDay();
            LocalDateTime finDia = fecha.atTime(23, 59, 59);
            Double monto = pedidoRepository.sumarIngresosEntreFechas(inicioDia, finDia);

            Map<String, Object> diaVenta = new HashMap<>();
            String fechaTexto = fecha.format(formatter);
            fechaTexto = fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1);
            diaVenta.put("fecha", fechaTexto);
            diaVenta.put("monto", monto != null ? monto : 0.0);
            ventasSemanales.add(diaVenta);
        }

        // Productos por categoría (sin findAll())
        List<Object[]> conteoCategorias = productoRepository.contarProductosPorCategoria();
        List<Map<String, Object>> categoriasList = new ArrayList<>();
        for (Object[] row : conteoCategorias) {
            String nombre = (String) row[0];
            Long cantidad = (Long) row[1];
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("nombre", nombre != null ? nombre : "Sin Categoría");
            catMap.put("cantidad", cantidad);
            categoriasList.add(catMap);
        }

        stats.put("ingresosHoy", ingHoy);
        stats.put("ingresosSemana", ingSemana);
        stats.put("ingresosMes", ingMes);
        stats.put("pendientes", pendientes);
        stats.put("totalProductos", totalProductos);
        stats.put("totalCategorias", totalCategorias);
        stats.put("ventasSemanales", ventasSemanales);
        stats.put("categorias", categoriasList);

        return ResponseEntity.ok(stats);
    }
}