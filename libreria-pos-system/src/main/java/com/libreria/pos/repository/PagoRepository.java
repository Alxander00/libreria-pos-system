package com.libreria.pos.repository;

import com.libreria.pos.entities.PagoEntity;
import com.libreria.pos.entities.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<PagoEntity, Long> {

    Optional<PagoEntity> findByPedido(PedidoEntity pedido);
}
