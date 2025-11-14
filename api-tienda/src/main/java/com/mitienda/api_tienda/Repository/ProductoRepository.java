package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // <-- ¡Importante!
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // <-- ¡Añade esta importación!

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * MÉTODO 1: Para la PÁGINA DE LISTA (con filtros)
     * Busca productos por nombre y/o categoría.
     */
    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN FETCH p.marca m " +
            "LEFT JOIN FETCH p.categoria c " +
            "WHERE " +
            "(COALESCE(:search, '') = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND " +
            "(COALESCE(:categoriaNombre, '') = '' OR c.nombre = :categoriaNombre)")
    List<Producto> findAllWithDetailsAndFilters(
            @Param("search") String search,
            @Param("categoriaNombre") String categoriaNombre
    );

    /**
     * MÉTODO 2: Para la PÁGINA DE DETALLE (¡El que faltaba!)
     * Trae un solo producto pero incluye (FETCH) la marca y la categoría
     * en la misma consulta para evitar el N+1 y el bucle.
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.marca LEFT JOIN FETCH p.categoria WHERE p.idProducto = :id")
    Optional<Producto> findByIdWithDetails(@Param("id") Integer id);
}