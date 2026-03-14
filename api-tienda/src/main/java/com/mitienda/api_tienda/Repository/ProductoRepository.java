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

    Optional<Producto> findTopByCodigoSkuStartingWithOrderByIdProductoDesc(String prefix);

    // --- NUEVA BÚSQUEDA EN CASCADA CON FAMILIA COMPLETA ---
    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN FETCH p.marca m " +
            "LEFT JOIN FETCH p.categoria c " +
            "LEFT JOIN FETCH c.categoriaPadre cp " +
            "LEFT JOIN FETCH cp.categoriaPadre cgp " +
            "LEFT JOIN p.variantes v " +
            "WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "  LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(p.codigoSku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(m.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(v.color) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "  LOWER(v.skuVariante) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND " +
            "(:categoriaNombre IS NULL OR :categoriaNombre = '' OR " +
            " c.nombre = :categoriaNombre OR cp.nombre = :categoriaNombre OR cgp.nombre = :categoriaNombre)")
    List<Producto> findAllWithDetailsAndFilters(@Param("search") String search, @Param("categoriaNombre") String categoriaNombre);

    // Busca productos que pertenezcan a una sucursal específica (buscando por el nombre, ej. "ropa")
    @Query("SELECT p FROM Producto p WHERE LOWER(p.sucursal.nombre) = LOWER(:nombreSucursal)")
    List<Producto> findBySucursalNombre(@Param("nombreSucursal") String nombreSucursal);

    @Query("SELECT p FROM Producto p " +
            "LEFT JOIN FETCH p.marca " +
            "LEFT JOIN FETCH p.categoria c " +
            "LEFT JOIN FETCH c.categoriaPadre cp " +
            "LEFT JOIN FETCH cp.categoriaPadre cgp " +
            "WHERE p.idProducto = :id")
    Optional<Producto> findByIdWithDetails(@Param("id") Integer id);
}