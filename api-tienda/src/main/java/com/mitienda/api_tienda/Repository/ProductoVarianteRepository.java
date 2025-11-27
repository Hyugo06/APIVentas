package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.ProductoVariante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoVarianteRepository extends JpaRepository<ProductoVariante, Integer> {
}