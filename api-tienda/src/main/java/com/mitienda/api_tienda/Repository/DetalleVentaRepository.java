package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    // Generalmente no necesita métodos personalizados
}