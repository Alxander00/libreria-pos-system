package com.libreria.pos.repository;

import com.libreria.pos.entities.PedidoDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalleEntity, Long> {
}
