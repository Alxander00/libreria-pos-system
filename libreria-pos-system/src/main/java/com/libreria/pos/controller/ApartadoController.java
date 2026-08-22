package com.libreria.pos.controller;

import com.libreria.pos.dto.AbonoRequest;
import com.libreria.pos.dto.ApartadoRequest;
import com.libreria.pos.dto.ApartadoResponse;
import com.libreria.pos.service.IApartado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/apartados")
public class ApartadoController {

    @Autowired
    private IApartado apartadoService;

    // === CLIENTE ===
    @PostMapping("/crear")
    public ResponseEntity<ApartadoResponse> crear(@RequestBody ApartadoRequest request) {
        return ResponseEntity.ok(apartadoService.crearApartado(request));
    }

    @PostMapping("/{id}/abonar")
    public ResponseEntity<ApartadoResponse> abonar(@PathVariable Long id, @RequestBody AbonoRequest request) {
        return ResponseEntity.ok(apartadoService.abonar(id, request));
    }

    @GetMapping("/mis-apartados")
    public ResponseEntity<List<ApartadoResponse>> misApartados() {
        return ResponseEntity.ok(apartadoService.obtenerMisApartados());
    }

    // === ADMIN ===
    @GetMapping("/admin/todos")
    public ResponseEntity<List<ApartadoResponse>> todos() {
        return ResponseEntity.ok(apartadoService.obtenerTodosApartados());
    }

    @PutMapping("/admin/{id}/cancelar")
    public ResponseEntity<ApartadoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(apartadoService.cancelar(id));
    }

    @PutMapping("/admin/{id}/liquidar")
    public ResponseEntity<ApartadoResponse> liquidar(@PathVariable Long id) {
        return ResponseEntity.ok(apartadoService.liquidar(id));
    }
}