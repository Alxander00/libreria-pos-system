package com.libreria.pos.controller;

import com.libreria.pos.dto.CierreCajaRequest;
import com.libreria.pos.dto.CorteHistorialDTO;
import com.libreria.pos.dto.CortePreviewDTO;
import com.libreria.pos.dto.CorteResponse;
import com.libreria.pos.service.ICorteCaja;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/corte")
public class CorteCajaController {

    @Autowired
    private ICorteCaja corteService;

    @GetMapping("/preview")
    public ResponseEntity<CortePreviewDTO> preview() {
        return ResponseEntity.ok(corteService.obtenerPreview());
    }

    @PostMapping("/cerrar")
    public ResponseEntity<CorteResponse> cerrar(@RequestBody CierreCajaRequest request) {
        return ResponseEntity.ok(corteService.cerrarCaja(request));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<CorteHistorialDTO>> historial(
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fin) {
        return ResponseEntity.ok(corteService.obtenerHistorial(inicio, fin));
    }
}