package com.tienda.ecommerce.repository;

import com.tienda.ecommerce.entities.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmail(String email);
    Page<UsuarioEntity> findByNombreContainingIgnoreCaseAndRol(String nombre, UsuarioEntity.Rol rol, Pageable pageable);
}
