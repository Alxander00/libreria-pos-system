package com.libreria.pos.controller;

import com.libreria.pos.entities.CategoriaEntity;
import com.libreria.pos.service.ICategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    @Autowired
    private ICategoria icategoria;

    @GetMapping
    public List<CategoriaEntity> findAll(){
        return icategoria.findAll();
    }

    @GetMapping("/{id}")
    public CategoriaEntity findById(@PathVariable Long id){
        return icategoria.findById(id);
    }

    @PostMapping
    public CategoriaEntity save(@RequestBody CategoriaEntity categoria){
        return icategoria.save(categoria);
    }

    @PutMapping("/{id}")
    public CategoriaEntity update(@PathVariable Long id, @RequestBody CategoriaEntity categoria){
        return icategoria.update(id, categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try {
            icategoria.delete(id);
            return ResponseEntity.ok("Categoría eliminada correctamente");
        } catch (Exception e) {
            // Si entra aquí, es probable que sea por restricción de llave foránea
            return ResponseEntity.badRequest().body("Error: No se puede eliminar porque esta categoría tiene productos asociados.");
        }
    }
}
