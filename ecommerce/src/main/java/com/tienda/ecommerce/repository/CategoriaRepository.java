package com.tienda.ecommerce.repository;

import com.tienda.ecommerce.entities.CategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {
    List<CategoriaEntity> findByActivoTrue();
}
