package com.libreria.pos.repository;

import com.libreria.pos.dto.ClienteFrecuenteDTO;
import com.libreria.pos.dto.IngresosPorCategoriaDTO;
import com.libreria.pos.dto.ProductoMasVendidoDTO;
import com.libreria.pos.entities.EstadoPedido;
import com.libreria.pos.entities.PedidoEntity;
import com.libreria.pos.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {

    // Métodos existentes
    List<PedidoEntity> findByUsuario(UsuarioEntity usuario);
    List<PedidoEntity> findByUsuarioIdUsuario(Long idUsuario);
    Long countByEstado(EstadoPedido estado);

    // Obtener pedidos solo con estado ENTREGADO o PAGADO en un rango de fechas
    @Query("SELECT p FROM PedidoEntity p WHERE (p.estado = 'ENTREGADO' OR p.estado = 'PAGADO') " +
            "AND p.fecha BETWEEN :inicio AND :fin")
    List<PedidoEntity> findVentasEntreFechas(@Param("inicio") LocalDateTime inicio,
                                             @Param("fin") LocalDateTime fin);

    // Obtener solo el total de ventas agrupado por método de pago (para corte de caja)
    @Query("SELECT p.metodoPago, SUM(p.total) FROM PedidoEntity p " +
            "WHERE (p.estado = 'ENTREGADO' OR p.estado = 'PAGADO') " +
            "AND p.fecha >= :fechaInicio " +
            "GROUP BY p.metodoPago")
    List<Object[]> sumarVentasPorMetodoPago(@Param("fechaInicio") LocalDateTime fechaInicio);

    // Obtener total de ingresos en un período
    @Query("SELECT COALESCE(SUM(p.total), 0) FROM PedidoEntity p " +
            "WHERE (p.estado = 'ENTREGADO' OR p.estado = 'PAGADO') " +
            "AND p.fecha BETWEEN :inicio AND :fin")
    Double sumarIngresosEntreFechas(@Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);

    // Productos más vendidos por cantidad
    @Query("SELECT NEW com.libreria.pos.dto.ProductoMasVendidoDTO(" +
            "p.idProducto, p.nombre, SUM(d.cantidad), SUM(d.precio * d.cantidad), p.categoria.nombre) " +
            "FROM PedidoDetalleEntity d JOIN d.producto p " +
            "WHERE d.pedido.estado IN ('ENTREGADO', 'PAGADO') " +
            "AND d.pedido.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY p.idProducto, p.nombre, p.categoria.nombre " +
            "ORDER BY SUM(d.cantidad) DESC")
    List<ProductoMasVendidoDTO> findProductosMasVendidosPorCantidad(@Param("inicio") LocalDateTime inicio,
                                                                    @Param("fin") LocalDateTime fin);

    // Productos más vendidos por monto
    @Query("SELECT NEW com.libreria.pos.dto.ProductoMasVendidoDTO(" +
            "p.idProducto, p.nombre, SUM(d.cantidad), SUM(d.precio * d.cantidad), p.categoria.nombre) " +
            "FROM PedidoDetalleEntity d JOIN d.producto p " +
            "WHERE d.pedido.estado IN ('ENTREGADO', 'PAGADO') " +
            "AND d.pedido.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY p.idProducto, p.nombre, p.categoria.nombre " +
            "ORDER BY SUM(d.precio * d.cantidad) DESC")
    List<ProductoMasVendidoDTO> findProductosMasVendidosPorMonto(@Param("inicio") LocalDateTime inicio,
                                                                 @Param("fin") LocalDateTime fin);

    // Clientes frecuentes
    @Query("SELECT NEW com.libreria.pos.dto.ClienteFrecuenteDTO(" +
            "u.idUsuario, u.nombre, u.email, COUNT(p), SUM(p.total)) " +
            "FROM PedidoEntity p JOIN p.usuario u " +
            "WHERE p.estado IN ('ENTREGADO', 'PAGADO') " +
            "AND p.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY u.idUsuario, u.nombre, u.email " +
            "ORDER BY COUNT(p) DESC")
    List<ClienteFrecuenteDTO> findClientesFrecuentes(@Param("inicio") LocalDateTime inicio,
                                                     @Param("fin") LocalDateTime fin);

    // Ingresos por categoría
    @Query("SELECT NEW com.libreria.pos.dto.IngresosPorCategoriaDTO(" +
            "p.categoria.nombre, SUM(d.precio * d.cantidad), SUM(d.cantidad)) " +
            "FROM PedidoDetalleEntity d JOIN d.producto p " +
            "WHERE d.pedido.estado IN ('ENTREGADO', 'PAGADO') " +
            "AND d.pedido.fecha BETWEEN :inicio AND :fin " +
            "GROUP BY p.categoria.nombre " +
            "ORDER BY SUM(d.precio * d.cantidad) DESC")
    List<IngresosPorCategoriaDTO> findIngresosPorCategoria(@Param("inicio") LocalDateTime inicio,
                                                           @Param("fin") LocalDateTime fin);
}