package com.libreria.pos.repository;

import com.libreria.pos.entities.CorteCajaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CorteCajaRepository extends JpaRepository<CorteCajaEntity, Long> {

    // ✅ Método para obtener el último corte por fecha de cierre (descendente)
    Optional<CorteCajaEntity> findFirstByOrderByFechaCierreDesc();

    // ✅ Método para obtener todos los cortes ordenados por fecha de cierre (desc)
    List<CorteCajaEntity> findAllByOrderByFechaCierreDesc();

    // ✅ Método para obtener cortes entre dos fechas
    @Query("SELECT c FROM CorteCajaEntity c WHERE c.fechaCierre BETWEEN :inicio AND :fin ORDER BY c.fechaCierre DESC")
    List<CorteCajaEntity> findAllBetweenDates(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}