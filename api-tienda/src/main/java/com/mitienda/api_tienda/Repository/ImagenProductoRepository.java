package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- ¡AÑADE ESTA IMPORTACIÓN!

@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Integer> {

    // --- ¡¡AÑADE ESTE MÉTODO!! ---
    /**
     * Spring Data JPA entiende este nombre y crea la consulta:
     * "Buscar por el campo 'producto', y dentro de ese objeto, por el campo 'idProducto'"
     */
    List<ImagenProducto> findByProductoIdProducto(Integer idProducto);
}