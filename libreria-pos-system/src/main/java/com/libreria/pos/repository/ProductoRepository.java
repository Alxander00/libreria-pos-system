package com.libreria.pos.repository;

import com.libreria.pos.entities.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    Page<ProductoEntity> findByActivoTrue(Pageable pageable);
    Page<ProductoEntity> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);
    Page<ProductoEntity> findByCategoriaIdCategoriaAndActivoTrue(Long idCategoria, Pageable pageable);
    Optional<ProductoEntity> findByCodigoBarras(String codigoBarras);

    // 👇 LA NUEVA SUPER CONSULTA PARA EL CATÁLOGO 👇
    @Query("SELECT p FROM ProductoEntity p WHERE p.activo = true AND " +
            "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) AND " +
            "(:categoria IS NULL OR p.categoria.nombre = :categoria) AND " +
            "(p.precio >= :minPrecio) AND " +
            "(:maxPrecio IS NULL OR p.precio <= :maxPrecio)")
    Page<ProductoEntity> buscarCatalogoAvanzado(
            @Param("texto") String texto,
            @Param("categoria") String categoria,
            @Param("minPrecio") Double minPrecio,
            @Param("maxPrecio") Double maxPrecio,
            Pageable pageable);

    // ========================================================
    // SUPER CONSULTAS PARA ACTUALIZACIÓN MASIVA (BULK UPDATES)
    // ========================================================

    @Modifying
    @Query("UPDATE ProductoEntity p SET p.descuento = :porcentaje WHERE p.activo = true")
    void actualizarDescuentoGlobal(@Param("porcentaje") Integer porcentaje);

    @Modifying
    @Query("UPDATE ProductoEntity p SET p.descuento = :porcentaje WHERE p.categoria.idCategoria = :idCat AND p.activo = true")
    void actualizarDescuentoPorCategoria(@Param("idCat") Long idCat, @Param("porcentaje") Integer porcentaje);
}