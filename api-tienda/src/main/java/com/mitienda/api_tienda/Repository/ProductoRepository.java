package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// ¡NO IMPORTAMOS NADA EXTRA!
// NI List, NI Optional, NI @Query, NI @Param

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ¡¡DEJA ESTE ARCHIVO VACÍO!!

    // JpaRepository ya nos da:
    // 1. findAll()
    // 2. findById()
    // 3. save()
    // 4. deleteById()
    // etc.

    // Borra cualquier método personalizado que te haya dado antes, como:
    // - findAllWithDetails()
    // - findByIdWithDetails()
}