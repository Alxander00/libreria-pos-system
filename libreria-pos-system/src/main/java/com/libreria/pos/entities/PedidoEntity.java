package com.libreria.pos.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "pedidos")
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedidos;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore
    private UsuarioEntity usuario;

    private Double total;

    @Column(name = "costo_envio")
    private Double costoEnvio;

    @Column(name = "metodo_entrega")
    private String metodoEntrega;

    @Column(name = "direccion")
    private String direccion;

    private LocalDateTime fecha;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<PedidoDetalleEntity> items = new ArrayList<>();

    @Column(name = "oculto_cliente", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean ocultoCliente = false;

    @Column(name = "oculto_admin")
    private Boolean ocultoAdmin = false;

    // --- DATOS FISCALES DEL MINISTERIO DE HACIENDA ---
    @Column(name = "codigo_generacion", unique = true)
    private String codigoGeneracion; // El UUID de la factura

    @Column(name = "numero_control", unique = true)
    private String numeroControl; // Tu correlativo interno (DTE-01-...)

    @Column(name = "sello_recibido")
    private String selloRecibido; // La firma de aprobación del gobierno
}