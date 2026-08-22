package com.libreria.pos.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto_variaciones")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductoVariacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVariacion;

    private String color;
    private Long stock;
    private String talla;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    @JsonBackReference // Esto es vital para evitar el bucle infinito del que hablamos antes
    private ProductoEntity producto;
}