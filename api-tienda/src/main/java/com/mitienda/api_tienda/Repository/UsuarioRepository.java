package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Método para encontrar un usuario por su nombre (útil para el login)
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Método para encontrar todos los usuarios que coincidan con un rol
    // ¡Este es el que usaremos para "obtener vendedores"!
    List<Usuario> findByRol(String rol);
}