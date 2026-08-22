package com.tienda.ecommerce.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "carrito_detalle")
public class CarritoDetalleEntity {

    @Id
    @Column(name = "id_detalle")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_carrito")
    @JsonIgnore
    private CarritoEntity carrito;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private ProductoEntity producto;

    @ManyToOne
    @JoinColumn(name = "id_variacion")
    private ProductoVariacionEntity variacion;

    private Long cantidad;
}
