package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Caja;
import com.mitienda.api_tienda.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {

    // Buscar si el usuario tiene una caja ABIERTA actualmente
    Optional<Caja> findByUsuarioAndEstado(Usuario usuario, String estado);
}