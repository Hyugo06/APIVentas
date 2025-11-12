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

    // --- ¡¡ARREGLO DE RENDIMIENTO N+1!! ---
    // (Cambiamos tus métodos originales para que usen JOIN FETCH)

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.id = :id")
    Optional<Venta> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.usuario.idUsuario = :idUsuario")
    List<Venta> findByUsuarioIdUsuarioWithDetails(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.cliente.idCliente = :idCliente")
    List<Venta> findByClienteIdClienteWithDetails(@Param("idCliente") Integer idCliente);

    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    List<Venta> findByFechaVentaBetweenWithDetails(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);


    // --- (Este método arreglaba la lista principal) ---
    @Query("SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.usuario LEFT JOIN FETCH v.cliente")
    List<Venta> findAllWithDetails();


    // --- ¡¡ARREGLO DE COMPILACIÓN!! ---
    // (Este método faltaba)
    @Query("SELECT SUM(v.montoTotal) FROM Venta v")
    BigDecimal calcularTotalVentas();
}