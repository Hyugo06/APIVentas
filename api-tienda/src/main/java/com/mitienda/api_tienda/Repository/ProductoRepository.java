package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ¡Y ya está! Con solo esto, ya tienes:
    // .findAll() -> (Obtener todos)
    // .findById(Integer id) -> (Buscar por ID)
    // .save(Producto producto) -> (Crear o Actualizar)
    // .deleteById(Integer id) -> (Borrar)

    // Puedes añadir métodos personalizados:
    // (Ej. Buscar producto por su SKU)
    Producto findByCodigoSku(String sku);
}