package com.libreria.pos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "apartados")
public class ApartadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idApartado;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity cliente;

    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private ProductoEntity producto;

    @ManyToOne
    @JoinColumn(name = "id_variacion")
    private ProductoVariacionEntity variacion;

    private Long cantidad = 1L; // Generalmente 1, pero flexible

    private Double totalAcordado;

    private Double montoPagado = 0.0;

    private Double saldoPendiente;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaLimite; // Fecha tope para pagar (opcional)

    @Enumerated(EnumType.STRING)
    private EstadoApartado estado;

    @OneToMany(mappedBy = "apartado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApartadoPagoEntity> pagos = new ArrayList<>();

    // Helper para agregar pago
    public void agregarPago(ApartadoPagoEntity pago) {
        pagos.add(pago);
        pago.setApartado(this);
        this.montoPagado += pago.getMonto();
        this.saldoPendiente = this.totalAcordado - this.montoPagado;
        if (this.saldoPendiente <= 0) {
            this.estado = EstadoApartado.LIQUIDADO;
        }
    }
}