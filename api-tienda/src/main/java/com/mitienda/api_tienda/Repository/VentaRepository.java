package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // Traemos: Venta -> Usuario, Cliente, Cupon, Detalles -> Producto, Variante
    @Query("SELECT v FROM Venta v " +
            "LEFT JOIN FETCH v.usuario " +
            "LEFT JOIN FETCH v.cliente " +
            "LEFT JOIN FETCH v.cupon " +
            "LEFT JOIN FETCH v.detalles d " +
            "LEFT JOIN FETCH d.producto " +
            "LEFT JOIN FETCH d.variante " +
            "WHERE v.idVenta = :id")
    Optional<Venta> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.usuario.idUsuario = :idUsuario")
    List<Venta> findByUsuarioIdUsuarioWithDetails(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.cliente.idCliente = :idCliente")
    List<Venta> findByClienteIdClienteWithDetails(@Param("idCliente") Integer idCliente);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    List<Venta> findByFechaVentaBetweenWithDetails(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente")
    List<Venta> findAllWithDetails();

    // --- CORRECCIÓN 1: Usamos v.montoTotal ---
    @Query("SELECT SUM(v.montoTotal) FROM Venta v")
    BigDecimal calcularTotalVentas();

    // --- CORRECCIÓN 2: Usamos v.montoTotal ---
    // Consultas para métricas
    @Query("SELECT COALESCE(SUM(v.montoTotal), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    BigDecimal sumMontoTotalBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Long countVentasBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Este ya estaba bien, pero lo mantenemos igual
    @Query("SELECT COALESCE(SUM(v.montoTotal), 0) FROM Venta v " +
            "WHERE v.usuario.idUsuario = :idUsuario " +
            "AND v.fechaVenta >= :fechaInicio " +
            "AND v.estado = 'COMPLETADA'")
    BigDecimal sumarVentasDelUsuarioDesde(@Param("idUsuario") Integer idUsuario,
                                          @Param("fechaInicio") LocalDateTime fechaInicio);
}