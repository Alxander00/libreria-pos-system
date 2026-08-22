package com.tienda.ecommerce.repository;

import com.tienda.ecommerce.entities.PedidoDetalleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalleEntity, Long> {
}
