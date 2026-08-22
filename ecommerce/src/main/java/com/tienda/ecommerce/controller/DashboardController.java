package com.tienda.ecommerce.controller;

import com.tienda.ecommerce.entities.PedidoEntity;
import com.tienda.ecommerce.entities.ProductoEntity;
import com.tienda.ecommerce.repository.PedidoRepository;
import com.tienda.ecommerce.repository.ProductoRepository;
import com.tienda.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
    private UsuarioRepository usuarioRepository;

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        List<PedidoEntity> pedidos = pedidoRepository.findAll();
        List<ProductoEntity> productos = productoRepository.findAll();

        LocalDate hoy = LocalDate.now();
        // Obtener el inicio de la semana (Lunes)
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        double ingHoy = 0, ingSemana = 0, ingMes = 0;
        int pendientes = 0;

        // Pre-llenar los últimos 7 días en un mapa para la gráfica
        Map<LocalDate, Double> ventasPorDia = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            ventasPorDia.put(hoy.minusDays(i), 0.0);
        }

        for (PedidoEntity p : pedidos) {
            // Ignoramos los pedidos que tú como admin ocultaste/archivaste
            if (p.getOcultoAdmin() != null && p.getOcultoAdmin()) {
                continue;
            }

            if ("PENDIENTE".equals(p.getEstado().name())) {
                pendientes++;
            }

            if ("ENTREGADO".equals(p.getEstado().name()) || "PAGADO".equals(p.getEstado().name())) {

                // 👇 ESTA ES LA CORRECCIÓN 👇
                // Como tu entidad ya usa LocalDateTime, la extracción es directa
                LocalDate fechaPedido = p.getFecha().toLocalDate();

                // Sumatoria de tarjetas
                if (fechaPedido.isEqual(hoy)) ingHoy += p.getTotal();
                if (!fechaPedido.isBefore(inicioSemana)) ingSemana += p.getTotal();
                if (!fechaPedido.isBefore(inicioMes)) ingMes += p.getTotal();

                // Sumatoria para la gráfica de 7 días
                if (ventasPorDia.containsKey(fechaPedido)) {
                    ventasPorDia.put(fechaPedido, ventasPorDia.get(fechaPedido) + p.getTotal());
                }
            }
        }

        // Formatear datos de ventas para que el Frontend (JS) lo lea fácil
        List<Map<String, Object>> ventasSemanales = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E d", new Locale("es", "ES"));

        ventasPorDia.forEach((fecha, monto) -> {
            Map<String, Object> diaVenta = new HashMap<>();
            // Capitalizar la primera letra del día (ej. "lun 12" -> "Lun 12")
            String fechaTexto = fecha.format(formatter);
            fechaTexto = fechaTexto.substring(0, 1).toUpperCase() + fechaTexto.substring(1);

            diaVenta.put("fecha", fechaTexto);
            diaVenta.put("monto", monto);
            ventasSemanales.add(diaVenta);
        });

        // Agrupar productos por categoría para la gráfica de Dona
        Map<String, Long> conteoCategorias = productos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin Categoría",
                        Collectors.counting()
                ));

        List<Map<String, Object>> categoriasList = new ArrayList<>();
        conteoCategorias.forEach((nombre, cantidad) -> {
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("nombre", nombre);
            catMap.put("cantidad", cantidad);
            categoriasList.add(catMap);
        });

        // Empaquetar todo el JSON de respuesta
        stats.put("ingresosHoy", ingHoy);
        stats.put("ingresosSemana", ingSemana);
        stats.put("ingresosMes", ingMes);
        stats.put("pendientes", pendientes);
        stats.put("totalProductos", productos.size());
        stats.put("totalCategorias", conteoCategorias.size());
        stats.put("ventasSemanales", ventasSemanales);
        stats.put("categorias", categoriasList);

        return ResponseEntity.ok(stats);
    }
}