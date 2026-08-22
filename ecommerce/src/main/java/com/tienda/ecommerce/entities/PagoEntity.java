package com.tienda.ecommerce.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagos")
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @OneToOne
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private PedidoEntity pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo")
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoPago estadoPago;

    private Double monto;

    private LocalDateTime fecha;
}
