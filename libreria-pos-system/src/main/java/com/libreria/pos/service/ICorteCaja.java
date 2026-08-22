package com.libreria.pos.service;

import com.libreria.pos.dto.CierreCajaRequest;
import com.libreria.pos.dto.CorteHistorialDTO;
import com.libreria.pos.dto.CortePreviewDTO;
import com.libreria.pos.dto.CorteResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ICorteCaja {
    CortePreviewDTO obtenerPreview();
    CorteResponse cerrarCaja(CierreCajaRequest request);
    List<CorteHistorialDTO> obtenerHistorial(LocalDateTime inicio, LocalDateTime fin);
}