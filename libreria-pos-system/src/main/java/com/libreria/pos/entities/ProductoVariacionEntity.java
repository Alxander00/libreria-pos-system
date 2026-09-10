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

    // NUEVO CAMPO: Stock reservado por apartados
    @Column(name = "stock_reservado", columnDefinition = "BIGINT DEFAULT 0")
    private Long stockReservado = 0L;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    @JsonBackReference
    private ProductoEntity producto;

    // Método para obtener stock disponible (total - reservado)
    public Long getStockDisponible() {
        return (stock != null ? stock : 0L) - (stockReservado != null ? stockReservado : 0L);
    }
}