package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- ¡Importar Query!
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // <-- ¡Importar BigDecimal!
import java.time.LocalDateTime; // <-- ¡Importar LocalDateTime!
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // --- MÉTODOS AÑADIDOS ---

    /**
     * Busca todas las ventas realizadas por un vendedor específico (por su ID).
     * Spring JPA entiende "findByUsuarioIdUsuario"
     */
    List<Venta> findByUsuarioIdUsuario(Integer idUsuario);

    /**
     * Busca todas las ventas asociadas a un cliente específico (por su ID).
     */
    List<Venta> findByClienteIdCliente(Integer idCliente);

    /**
     * Busca todas las ventas que ocurrieron entre dos fechas.
     */
    List<Venta> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Consulta personalizada (JPQL) para sumar el 'montoTotal' de TODAS las ventas.
     * @Query nos permite escribir consultas más complejas.
     */
    @Query("SELECT SUM(v.montoTotal) FROM Venta v")
    BigDecimal calcularTotalVentas();
}