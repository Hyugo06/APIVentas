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

    // --- ¡CORRECCIÓN IMPORTANTE AQUÍ! ---
    // Traemos: Venta -> Usuario, Cliente, Detalles -> Producto, Variante
    @Query("SELECT v FROM Venta v " +
            "LEFT JOIN FETCH v.usuario " +
            "LEFT JOIN FETCH v.cliente " +
            "LEFT JOIN FETCH v.detalles d " +      // <-- Traer lista de detalles
            "LEFT JOIN FETCH d.producto " +        // <-- Traer producto de cada detalle
            "LEFT JOIN FETCH d.variante " +        // <-- ¡TRAER LA VARIANTE! (Color/Talla)
            "WHERE v.idVenta = :id")
    Optional<Venta> findByIdWithDetails(@Param("id") Integer id);
    // ------------------------------------

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.usuario.idUsuario = :idUsuario")
    List<Venta> findByUsuarioIdUsuarioWithDetails(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.cliente.idCliente = :idCliente")
    List<Venta> findByClienteIdClienteWithDetails(@Param("idCliente") Integer idCliente);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    List<Venta> findByFechaVentaBetweenWithDetails(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente")
    List<Venta> findAllWithDetails();

    @Query("SELECT SUM(v.montoTotal) FROM Venta v")
    BigDecimal calcularTotalVentas();

    // Consultas para métricas
    @Query("SELECT COALESCE(SUM(v.montoTotal), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    BigDecimal sumMontoTotalBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin")
    Long countVentasBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}