package com.libreria.pos.controller;

import com.libreria.pos.dto.ListaEscolarRequest;
import com.libreria.pos.dto.ListaEscolarResponse;
import com.libreria.pos.service.IListaEscolar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lista")
public class ListaController {

    @Autowired
    private IListaEscolar listaService;

    // ===== CLIENTE =====
    @PostMapping("/crear")
    public ResponseEntity<ListaEscolarResponse> crearLista(@RequestBody ListaEscolarRequest request) {
        return ResponseEntity.ok(listaService.crearLista(request));
    }

    @GetMapping("/mis-listas")
    public ResponseEntity<List<ListaEscolarResponse>> obtenerMisListas() {
        return ResponseEntity.ok(listaService.obtenerMisListas());
    }

    // ===== ADMIN =====
    @GetMapping("/admin/todas")
    public ResponseEntity<List<ListaEscolarResponse>> obtenerTodas() {
        return ResponseEntity.ok(listaService.obtenerTodasLasListas());
    }

    @PutMapping("/admin/{id}/armar")
    public ResponseEntity<ListaEscolarResponse> armarLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.armarLista(id));
    }

    @PutMapping("/admin/{id}/retirar")
    public ResponseEntity<ListaEscolarResponse> retirarLista(@PathVariable Long id) {
        return ResponseEntity.ok(listaService.retirarLista(id));
    }

    // Obtener una lista específica (para editar)
    @GetMapping("/{id}")
    public ResponseEntity<ListaEscolarResponse> obtenerLista(@PathVariable Long id) {
        // Implementar en el servicio
        return ResponseEntity.ok(listaService.obtenerLista(id));
    }

    // Actualizar una lista (cliente o admin)
    @PutMapping("/{id}")
    public ResponseEntity<ListaEscolarResponse> actualizarLista(@PathVariable Long id, @RequestBody ListaEscolarRequest request) {
        return ResponseEntity.ok(listaService.actualizarLista(id, request));
    }
}