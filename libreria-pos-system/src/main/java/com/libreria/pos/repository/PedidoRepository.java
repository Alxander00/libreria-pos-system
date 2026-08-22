package com.libreria.pos.repository;

import com.libreria.pos.entities.EstadoPedido;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByUsuario(UsuarioEntity usuario);
    List<PedidoEntity> findByUsuarioIdUsuario(Long idUsuario);
    Long countByEstado(EstadoPedido estado);
}
