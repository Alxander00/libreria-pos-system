package com.libreria.pos.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libreria.pos.dto.DescuentoRequest;
import com.libreria.pos.dto.ProductoRequest;
import com.libreria.pos.entities.CategoriaEntity;
import com.libreria.pos.entities.ProductoEntity;
import com.libreria.pos.entities.ProductoVariacionEntity;
import com.libreria.pos.repository.CategoriaRepository;
import com.libreria.pos.repository.ProductoRepository;
import com.libreria.pos.service.IProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductoImpl implements IProducto {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Page<ProductoEntity> findAll(Pageable pageable){
        return productoRepository.findByActivoTrue(pageable);
    }

    @Override
    public Page<ProductoEntity> searchByNombre(String nombre, Pageable pageable){
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre, pageable);
    }

    @Override
    public Page<ProductoEntity> findByCategoria(Long idCategoria, Pageable pageable){
        return productoRepository.findByCategoriaIdCategoriaAndActivoTrue(idCategoria, pageable);
    }

    @Override
    public ProductoEntity findById(Long id){
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    public ProductoEntity create(ProductoRequest request) {
        CategoriaEntity categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));

        ProductoEntity producto = new ProductoEntity();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }

    @Override
    public ProductoEntity update(Long id, ProductoRequest request) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        CategoriaEntity categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no existe"));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }

    @Override
    public void delete(Long id) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // NUEVO: Borrar imágenes de Cloudinary al eliminar el producto
        if (producto.getImagenesUrls() != null) {
            for (String urlAntigua : producto.getImagenesUrls()) {
                String publicId = extraerPublicId(urlAntigua);
                if (publicId != null) {
                    try {
                        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    } catch (Exception e) {
                        System.out.println("Error al borrar imagen en Cloudinary: " + e.getMessage());
                    }
                }
            }
        }

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // ========================================================
    // MÉTODOS AVANZADOS CON IMÁGENES Y VARIACIONES (COLORES)
    // ========================================================

    @Override
    public ProductoEntity createConImagen(String nombre, Double precio, String descripcion,
                                          String variacionesStr, Long categoriaId, MultipartFile[] imagenes) {

        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        try {
            List<String> urlsSubidas = new ArrayList<>();

            if (imagenes != null) {
                for (MultipartFile imagen : imagenes) {
                    if (!imagen.isEmpty()) {
                        //  NUEVO: Configuración de la carpeta
                        Map<String, Object> opciones = ObjectUtils.asMap("folder", "libreria_pos/productos");
                        Map uploadResult = cloudinary.uploader().upload(imagen.getBytes(), opciones);
                        urlsSubidas.add(uploadResult.get("url").toString());
                    }
                }
            }

            ProductoEntity producto = new ProductoEntity();
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setPrecio(precio);
            producto.setImagenesUrls(urlsSubidas);
            producto.setCategoria(categoria);

            if (variacionesStr != null && !variacionesStr.isEmpty()) {
                List<Map<String, Object>> variacionesList = objectMapper.readValue(variacionesStr, new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> vMap : variacionesList) {
                    ProductoVariacionEntity v = new ProductoVariacionEntity();
                    v.setColor(vMap.get("color").toString());
                    v.setTalla(vMap.get("talla") != null ? vMap.get("talla").toString() : "Única");
                    v.setStock(Long.parseLong(vMap.get("stock").toString()));
                    producto.agregarVariacion(v);
                }
            }

            return productoRepository.save(producto);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error guardando el producto: " + e.getMessage());
        }
    }

    @Override
    public ProductoEntity updateConImagen(Long id, String nombre, Double precio, String descripcion,
                                          String variacionesStr, Long categoriaId, MultipartFile[] imagenes) {

        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);

        try {
            if (variacionesStr != null && !variacionesStr.isEmpty()) {
                List<Map<String, Object>> variacionesList = objectMapper.readValue(variacionesStr, new TypeReference<List<Map<String, Object>>>() {});
                List<ProductoVariacionEntity> variacionesActualizadas = new ArrayList<>();

                for (Map<String, Object> vMap : variacionesList) {
                    Long idVar = vMap.get("idVariacion") != null ? Long.parseLong(vMap.get("idVariacion").toString()) : null;
                    String color = vMap.get("color").toString();
                    String talla = vMap.get("talla") != null ? vMap.get("talla").toString() : "Única";
                    Long stock = Long.parseLong(vMap.get("stock").toString());

                    ProductoVariacionEntity v;

                    if (idVar != null) {
                        v = producto.getVariaciones().stream()
                                .filter(existing -> existing.getIdVariacion().equals(idVar))
                                .findFirst()
                                .orElse(new ProductoVariacionEntity());
                    } else {
                        v = new ProductoVariacionEntity();
                        v.setProducto(producto);
                    }

                    v.setColor(color);
                    v.setTalla(talla);
                    v.setStock(stock);

                    variacionesActualizadas.add(v);
                }

                producto.getVariaciones().clear();
                producto.getVariaciones().addAll(variacionesActualizadas);
            }

            // NUEVO: Si mandaron fotos nuevas, primero borramos las viejas
            if (imagenes != null && imagenes.length > 0 && !imagenes[0].isEmpty()) {
                if (producto.getImagenesUrls() != null) {
                    for (String urlAntigua : producto.getImagenesUrls()) {
                        String publicId = extraerPublicId(urlAntigua);
                        if (publicId != null) {
                            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                        }
                    }
                }

                List<String> urlsSubidas = new ArrayList<>();
                for (MultipartFile imagen : imagenes) {
                    if (!imagen.isEmpty()) {
                        // NUEVO: Configuración de la carpeta
                        Map<String, Object> opciones = ObjectUtils.asMap("folder", "libreria_pos/productos");
                        Map uploadResult = cloudinary.uploader().upload(imagen.getBytes(), opciones);
                        urlsSubidas.add(uploadResult.get("url").toString());
                    }
                }
                producto.setImagenesUrls(urlsSubidas);
            }

            return productoRepository.save(producto);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error actualizando el producto: " + e.getMessage());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    @Override
    public void aplicarDescuentoPro(DescuentoRequest request) {
        if (request.getValor() == null || request.getValor() < 0 || request.getValor() > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100%");
        }
        if (request.getTipo() == null || request.getTipo().isEmpty()) {
            throw new IllegalArgumentException("El tipo de descuento es obligatorio");
        }

        switch (request.getTipo().toUpperCase()) {
            case "GLOBAL":
                productoRepository.actualizarDescuentoGlobal(request.getValor());
                break;

            case "CATEGORIA":
                if (request.getId() == null) throw new IllegalArgumentException("Se requiere el ID de la categoría");
                productoRepository.actualizarDescuentoPorCategoria(request.getId(), request.getValor());
                break;

            case "PRODUCTO":
                if (request.getId() == null) throw new IllegalArgumentException("Se requiere el ID del producto");
                ProductoEntity p = productoRepository.findById(request.getId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado en la base de datos"));
                p.setDescuento(request.getValor().longValue());
                productoRepository.save(p);
                break;

            default:
                throw new IllegalArgumentException("Tipo de descuento no soportado: " + request.getTipo());
        }
    }

    // NUEVO: Método auxiliar para extraer el ID de la imagen
    private String extraerPublicId(String urlImagen) {
        try {
            int startIndex = urlImagen.indexOf("libreria_pos/productos/");
            if (startIndex != -1) {
                String rutaYArchivo = urlImagen.substring(startIndex);
                int dotIndex = rutaYArchivo.lastIndexOf('.');
                return (dotIndex != -1) ? rutaYArchivo.substring(0, dotIndex) : rutaYArchivo;
            }
        } catch (Exception e) {
            System.out.println("Error extrayendo public_id de: " + urlImagen);
        }
        return null;
    }
}