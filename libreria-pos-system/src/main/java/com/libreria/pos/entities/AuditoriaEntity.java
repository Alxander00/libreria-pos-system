package com.libreria.pos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "auditoria")
@EntityListeners(AuditingEntityListener.class)
public class AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditoria;

    @Column(nullable = false)
    private String usuarioEmail;

    @Column(nullable = false)
    private String accion;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @Column(name = "ip_cliente")
    private String ipCliente;

    @Column(name = "endpoint")
    private String endpoint;

    @CreatedDate
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;
}