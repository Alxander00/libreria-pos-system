package com.libreria.pos.repository;

import com.libreria.pos.entities.ApartadoEntity;
import com.libreria.pos.entities.EstadoApartado;
import com.libreria.pos.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartadoRepository extends JpaRepository<ApartadoEntity, Long> {
    List<ApartadoEntity> findByCliente(UsuarioEntity cliente);
    List<ApartadoEntity> findByEstadoOrderByFechaCreacionDesc(EstadoApartado estado);
}