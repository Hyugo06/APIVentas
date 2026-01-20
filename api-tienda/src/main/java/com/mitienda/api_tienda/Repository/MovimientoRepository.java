package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {
    // Buscar todos los movimientos de un cliente específico
    List<Movimiento> findByCliente_IdCliente(Integer idCliente);
}