package com.tienda.ecommerce.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
    private double precio;

    // AQUÍ SE DECLARA LA VARIABLE QUE INTELLIJ "NO ENCUENTRA"
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoVariacionEntity> variaciones = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "producto_imagenes", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "imagen_url")
    private List<String> imagenesUrls = new ArrayList<>();

    @Column(name = "activo")
    private Boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private CategoriaEntity categoria;

    @Column(name = "descuento")
    private long descuento = 0; // Se inicializa en 0 para evitar errores de null

    // ==========================================
    // MÉTODOS MANUALES DE AYUDA Y PUENTE
    // ==========================================

    public void agregarVariacion(ProductoVariacionEntity variacion) {
        this.variaciones.add(variacion);
        variacion.setProducto(this);
    }

    public Long getStock() {
        if (this.variaciones == null || this.variaciones.isEmpty()) {
            return 0L;
        }
        long total = 0;
        for (ProductoVariacionEntity v : this.variaciones) {
            total += v.getStock();
        }
        return total;
    }

    public void setStock(Long stock) {
        if (this.variaciones == null) {
            this.variaciones = new ArrayList<>();
        }

        if (this.variaciones.isEmpty()) {
            ProductoVariacionEntity defaultVar = new ProductoVariacionEntity();
            defaultVar.setColor("Único");
            defaultVar.setStock(stock);
            defaultVar.setProducto(this);
            this.variaciones.add(defaultVar);
        } else {
            this.variaciones.get(0).setStock(stock);
        }
    }
}