package com.tienda.ecommerce.repository;

import com.tienda.ecommerce.entities.EstadoPedido;
import com.tienda.ecommerce.entities.PedidoEntity;
import com.tienda.ecommerce.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByUsuario(UsuarioEntity usuario);
    List<PedidoEntity> findByUsuarioIdUsuario(Long idUsuario);
    Long countByEstado(EstadoPedido estado);
}
