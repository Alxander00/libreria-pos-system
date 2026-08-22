package com.libreria.pos.service;

import com.libreria.pos.dto.DescuentoRequest;
import com.libreria.pos.dto.ProductoRequest;
import com.libreria.pos.entities.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IProducto {
    Page<ProductoEntity> findAll(Pageable pageable);
    Page<ProductoEntity> searchByNombre(String nombre, Pageable pageable);
    Page<ProductoEntity> findByCategoria(Long categoria, Pageable pageable);

    ProductoEntity findById(Long id);

    ProductoEntity create(ProductoRequest request);
    ProductoEntity update(Long id, ProductoRequest request);
    void delete(Long id);

    ProductoEntity createConImagen(
            String nombre, Double precio, String descripcion,
            String variacionesStr, Long categoriaId, MultipartFile[] imagenes
    );

    ProductoEntity updateConImagen(
            Long id, String nombre, Double precio, String descripcion,
            String variacionesStr, Long categoriaId, MultipartFile[] imagenes
    );

    // 👇 El método maestro Pro
    void aplicarDescuentoPro(DescuentoRequest request);
}