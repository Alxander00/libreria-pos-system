package com.libreria.pos.repository;

import com.libreria.pos.entities.ListaEscolarEntity;
import com.libreria.pos.entities.UsuarioEntity;
import com.libreria.pos.entities.EstadoLista; // 👈 IMPORTA EL ENUM
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListaEscolarRepository extends JpaRepository<ListaEscolarEntity, Long> {
    List<ListaEscolarEntity> findByUsuario(UsuarioEntity usuario);

    List<ListaEscolarEntity> findByEstadoOrderByFechaCreacionDesc(EstadoLista estado);
}