package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    // Por ahora no necesita métodos personalizados
    // JpaRepository ya nos da .findById(), .findAll(), .save(), etc.
}