package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // --- NUEVO MÉTODO MÁGICO (Reemplaza al @Query manual) ---
    // Spring crea la consulta automáticamente: "Busca el primero, ordenado por ID desc, que empiece con..."
    Optional<Producto> findTopByCodigoSkuStartingWithOrderByIdProductoDesc(String prefix);

    // --- TUS MÉTODOS EXISTENTES ---
    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN FETCH p.marca m " +
            "LEFT JOIN FETCH p.categoria c " +
            "LEFT JOIN p.variantes v " + // Unimos las variantes para buscar colores/tallas
            "WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "  LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(p.codigoSku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(m.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(v.color) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(v.skuVariante) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND " +
            "(:categoriaNombre IS NULL OR :categoriaNombre = '' OR c.nombre = :categoriaNombre)")
    List<Producto> findAllWithDetailsAndFilters(@Param("search") String search, @Param("categoriaNombre") String categoriaNombre);

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.marca LEFT JOIN FETCH p.categoria WHERE p.idProducto = :id")
    Optional<Producto> findByIdWithDetails(@Param("id") Integer id);
}