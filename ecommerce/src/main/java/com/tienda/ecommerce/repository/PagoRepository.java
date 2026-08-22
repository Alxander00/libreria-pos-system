package com.tienda.ecommerce.repository;

import com.tienda.ecommerce.entities.PagoEntity;
import com.tienda.ecommerce.entities.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<PagoEntity, Long> {

    Optional<PagoEntity> findByPedido(PedidoEntity pedido);
}
