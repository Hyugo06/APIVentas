package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    // Por ahora no necesita métodos personalizados
}