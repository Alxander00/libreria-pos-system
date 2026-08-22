package com.libreria.pos.service;

import com.libreria.pos.entities.CategoriaEntity;

import java.util.List;

public interface ICategoria {
    List<CategoriaEntity> findAll();
    CategoriaEntity findById(Long id);
    CategoriaEntity save(CategoriaEntity categoria);
    CategoriaEntity update(Long id, CategoriaEntity categoria);
    void delete(Long id);
}
