package com.libreria.pos.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApartadoResponse {
    private Long idApartado;
    private String nombreCliente;
    private String emailCliente;
    private String nombreProducto;
    private String variacionNombre;
    private Long cantidad;
    private Double totalAcordado;
    private Double montoPagado;
    private Double saldoPendiente;
    private String estado;
    private LocalDateTime fechaCreacion;
    private List<PagoResponse> pagos;

    @Data
    public static class PagoResponse {
        private Double monto;
        private LocalDateTime fecha;
        private String metodoPago;
    }
}