package com.libreria.pos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cortes_caja")
public class CorteCajaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCorte;

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    private Double totalEfectivo;
    private Double totalTarjeta;
    private Double totalTransferencia;
    private Double totalGeneral;

    private Double efectivoEnCaja; // Lo que el admin cuenta físicamente
    private Double diferencia; // efectivoEnCaja - totalEfectivo (puede ser negativo)

    @Enumerated(EnumType.STRING)
    private EstadoCorte estado; // ABIERTO, CERRADO

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private UsuarioEntity usuarioCierre; // Quién cerró la caja
}