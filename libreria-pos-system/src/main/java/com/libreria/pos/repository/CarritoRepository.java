package com.libreria.pos.repository;

import com.libreria.pos.entities.CarritoEntity;
import com.libreria.pos.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<CarritoEntity, Long> {

    Optional<CarritoEntity> findByUsuario(UsuarioEntity usuario);
}
