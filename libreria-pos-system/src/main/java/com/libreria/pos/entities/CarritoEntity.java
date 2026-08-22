package com.libreria.pos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "carrito")
public class CarritoEntity {

    @Id
    @Column(name = "id_carrito")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCarrito;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private UsuarioEntity usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarritoDetalleEntity> items = new ArrayList<>();

    @Transient
    public Double getTotal() {
        return items.stream().mapToDouble(i -> {
            double precioReal = i.getProducto().getPrecio();
            long descuento = i.getProducto().getDescuento(); // Traemos el descuento de la base de datos

            // Si tiene descuento, aplicamos la rebaja matemática
            if (descuento > 0) {
                precioReal = precioReal - (precioReal * (descuento / 100.0));
            }

            return precioReal * i.getCantidad();
        }).sum();
    }
}
