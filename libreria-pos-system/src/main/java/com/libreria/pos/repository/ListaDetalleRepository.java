package com.libreria.pos.repository;

import com.libreria.pos.entities.ListaDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaDetalleRepository extends JpaRepository<ListaDetalleEntity, Long> {
}