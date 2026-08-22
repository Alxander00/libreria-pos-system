package com.tienda.ecommerce.repository;

import com.tienda.ecommerce.entities.CarritoEntity;
import com.tienda.ecommerce.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<CarritoEntity, Long> {

    Optional<CarritoEntity> findByUsuario(UsuarioEntity usuario);
}
