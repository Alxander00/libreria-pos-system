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
@Table(name = "listas_escolares")
public class ListaEscolarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLista;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    private String grado; // Ej: "Primero", "Segundo", "Universidad"
    private String anio;  // Ej: "2026"
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    private EstadoLista estado; // PENDIENTE, ARMADO, RETIRADO

    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListaDetalleEntity> detalles = new ArrayList<>();

    // Método helper
    public void agregarDetalle(ListaDetalleEntity detalle) {
        detalles.add(detalle);
        detalle.setLista(this);
    }
}