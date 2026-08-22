package com.libreria.pos.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

public interface IReporteService {
    ByteArrayInputStream generarExcelVentas(LocalDateTime desde, LocalDateTime hasta);
}