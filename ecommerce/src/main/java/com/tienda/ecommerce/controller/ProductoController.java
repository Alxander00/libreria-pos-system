package com.tienda.ecommerce.controller;

import com.tienda.ecommerce.dto.ProductoRequest;
import com.tienda.ecommerce.entities.ProductoEntity;
import com.tienda.ecommerce.service.IProducto;
import com.tienda.ecommerce.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/producto")
public class ProductoController {

    @Autowired
    private IProducto iproducto;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public Page<ProductoEntity> getProductos(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);

        if (categoria != null) {
            return iproducto.findByCategoria(categoria, pageable);
        }

        if (!search.isEmpty()) {
            return iproducto.searchByNombre(search, pageable);
        }

        return iproducto.findAll(pageable);
    }

    // 👇 EL NUEVO ENDPOINT PARA EL CATÁLOGO CON PAGINACIÓN 👇
    @GetMapping("/catalogo")
    public Page<ProductoEntity> obtenerCatalogo(
            @RequestParam(defaultValue = "") String buscar,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") Double minPrecio,
            @RequestParam(required = false) Double maxPrecio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "") String orden) {

        Sort sort = Sort.unsorted();
        if ("menor".equals(orden)) {
            sort = Sort.by("precio").ascending();
        } else if ("mayor".equals(orden)) {
            sort = Sort.by("precio").descending();
        }

        String catFinal = (categoria != null && !categoria.isEmpty()) ? categoria : null;

        return productoRepository.buscarCatalogoAvanzado(
                buscar,
                catFinal,
                minPrecio,
                maxPrecio,
                PageRequest.of(page, size, sort)
        );
    }

    @PostMapping("/imagen")
    public ProductoEntity crearConImagen(
            @RequestParam String nombre,
            @RequestParam Double precio,
            @RequestParam String variacionesStr,
            @RequestParam String descripcion,
            @RequestParam Long categoriaId,
            @RequestParam("imagenes") MultipartFile[] imagenes
    ) {
        return iproducto.createConImagen(
                nombre, precio, descripcion, variacionesStr, categoriaId, imagenes
        );
    }

    @PutMapping("/{id}")
    public ProductoEntity updateProducto(@PathVariable Long id, @RequestBody ProductoRequest request){
        return iproducto.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteProducto(@PathVariable Long id){
        iproducto.delete(id);
        return "Producto eliminado con exito";
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoEntity> getProducto(@PathVariable Long id){
        ProductoEntity producto = iproducto.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(producto);
    }

    @PutMapping("/editar-con-imagen/{id}")
    public ProductoEntity updateProductoConImagen(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam Double precio,
            @RequestParam String variacionesStr,
            @RequestParam String descripcion,
            @RequestParam Long categoriaId,
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes
    ) {
        return iproducto.updateConImagen(id, nombre, precio, descripcion, variacionesStr, categoriaId, imagenes);
    }
}