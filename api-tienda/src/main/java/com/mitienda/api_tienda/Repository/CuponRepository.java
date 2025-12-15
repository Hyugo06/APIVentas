package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Cupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Integer> {

    // Método mágico para buscar por el texto del código
    Optional<Cupon> findByCodigo(String codigo);
}