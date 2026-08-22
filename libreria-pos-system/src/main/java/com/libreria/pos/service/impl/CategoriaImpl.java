package com.libreria.pos.service.impl;

import com.libreria.pos.entities.CategoriaEntity;
import com.libreria.pos.repository.CategoriaRepository;
import com.libreria.pos.service.ICategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaImpl implements ICategoria {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<CategoriaEntity> findAll() {
        return categoriaRepository.findByActivoTrue();
    }

    @Override
    public CategoriaEntity findById(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
    }

    @Override
    public CategoriaEntity save(CategoriaEntity categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    public void delete(Long id) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    @Override
    public CategoriaEntity update(Long id, CategoriaEntity categoriaData) {
        CategoriaEntity categoria = findById(id);

        categoria.setNombre(categoriaData.getNombre());
        return categoriaRepository.save(categoria);
    }
}
