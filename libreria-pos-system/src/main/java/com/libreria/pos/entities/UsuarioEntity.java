package com.libreria.pos.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    private String nombre;

    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    private String direccion;
    private String telefono;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @JsonIgnore // Importante para evitar bucles infinitos en el JSON
    private List<PedidoEntity> pedidos;


    public int getTotalPedidos() {
        return (pedidos != null) ? pedidos.size() : 0;
    }

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Rol rol;

    public enum Rol {
        ADMIN,
        CLIENTE
    }

    private String fotoUrl;
}
