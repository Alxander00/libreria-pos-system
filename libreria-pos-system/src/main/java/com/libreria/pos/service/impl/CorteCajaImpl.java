package com.libreria.pos.service.impl;

import com.libreria.pos.dto.CierreCajaRequest;
import com.libreria.pos.dto.CorteHistorialDTO;
import com.libreria.pos.dto.CortePreviewDTO;
import com.libreria.pos.dto.CorteResponse;
import com.libreria.pos.entities.CorteCajaEntity;
import com.libreria.pos.entities.EstadoCorte;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.repository.CorteCajaRepository;
import com.libreria.pos.repository.PedidoRepository;
import com.libreria.pos.service.AuthService;
import com.libreria.pos.service.ICorteCaja;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
public class CorteCajaImpl implements ICorteCaja {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CorteCajaRepository corteRepository;

    @Autowired
    private AuthService authService;

    @Override
    public CortePreviewDTO obtenerPreview() {
        // 1. Obtener fecha de inicio desde el último corte
        LocalDateTime fechaInicio = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        Optional<CorteCajaEntity> ultimoCorte = corteRepository.findFirstByOrderByFechaCierreDesc();
        if (ultimoCorte.isPresent()) {
            fechaInicio = ultimoCorte.get().getFechaCierre();
        }

        // 2. Obtener totales por método de pago (una sola consulta)
        List<Object[]> resultados = pedidoRepository.sumarVentasPorMetodoPago(fechaInicio);
        double efectivo = 0, tarjeta = 0, transferencia = 0;

        for (Object[] fila : resultados) {
            String metodo = (String) fila[0];
            Double total = (Double) fila[1];
            if (metodo == null) continue;
            switch (metodo.toUpperCase()) {
                case "EFECTIVO": efectivo = total; break;
                case "TARJETA": tarjeta = total; break;
                case "TRANSFERENCIA": transferencia = total; break;
            }
        }

        // 3. Construir respuesta
        CortePreviewDTO dto = new CortePreviewDTO();
        dto.setTotalEfectivo(efectivo);
        dto.setTotalTarjeta(tarjeta);
        dto.setTotalTransferencia(transferencia);
        dto.setTotalGeneral(efectivo + tarjeta + transferencia);
        dto.setFechaInicio(fechaInicio);
        dto.setFechaFin(LocalDateTime.now());
        return dto;
    }

    @Override
    @Transactional
    public CorteResponse cerrarCaja(CierreCajaRequest request) {
        // Validaciones
        if (request == null || request.getEfectivoEnCaja() == null || request.getEfectivoEnCaja() < 0) {
            throw new RuntimeException("Debes ingresar el monto de efectivo contado en caja.");
        }

        CortePreviewDTO preview = obtenerPreview();
        double diferencia = request.getEfectivoEnCaja() - preview.getTotalEfectivo();

        // Guardar corte
        CorteCajaEntity corte = new CorteCajaEntity();
        corte.setFechaApertura(preview.getFechaInicio());
        corte.setFechaCierre(LocalDateTime.now());
        corte.setTotalEfectivo(preview.getTotalEfectivo());
        corte.setTotalTarjeta(preview.getTotalTarjeta());
        corte.setTotalTransferencia(preview.getTotalTransferencia());
        corte.setTotalGeneral(preview.getTotalGeneral());
        corte.setEfectivoEnCaja(request.getEfectivoEnCaja());
        corte.setDiferencia(diferencia);
        corte.setEstado(EstadoCorte.CERRADO);
        corte.setUsuarioCierre(authService.getUsuarioAutenticado());

        corteRepository.save(corte);

        // Mensaje de diferencia
        String mensajeDiff;
        if (diferencia > 0) mensajeDiff = "Sobrante de $" + String.format("%.2f", diferencia);
        else if (diferencia < 0) mensajeDiff = "Faltante de $" + String.format("%.2f", Math.abs(diferencia));
        else mensajeDiff = "Efectivo cuadra perfectamente";

        CorteResponse response = new CorteResponse();
        response.setMensaje("Corte cerrado exitosamente. " + mensajeDiff);
        response.setIdCorte(corte.getIdCorte());
        response.setTotalGeneral(corte.getTotalGeneral());
        response.setEfectivoEnCaja(corte.getEfectivoEnCaja());
        response.setDiferencia(corte.getDiferencia());
        return response;
    }

    @Override
    public List<CorteHistorialDTO> obtenerHistorial(LocalDateTime inicio, LocalDateTime fin) {
        List<CorteCajaEntity> cortes;
        if (inicio != null && fin != null) {
            cortes = corteRepository.findAllBetweenDates(inicio, fin);
        } else {
            cortes = corteRepository.findAllByOrderByFechaCierreDesc();
        }
        return cortes.stream().map(c -> {
            CorteHistorialDTO dto = new CorteHistorialDTO();
            dto.setIdCorte(c.getIdCorte());
            dto.setFechaApertura(c.getFechaApertura());
            dto.setFechaCierre(c.getFechaCierre());
            dto.setTotalEfectivo(c.getTotalEfectivo());
            dto.setTotalTarjeta(c.getTotalTarjeta());
            dto.setTotalTransferencia(c.getTotalTransferencia());
            dto.setTotalGeneral(c.getTotalGeneral());
            dto.setEfectivoEnCaja(c.getEfectivoEnCaja());
            dto.setDiferencia(c.getDiferencia());
            dto.setUsuarioCierre(c.getUsuarioCierre() != null ? c.getUsuarioCierre().getNombre() : "Sistema");
            return dto;
        }).toList();
    }
}