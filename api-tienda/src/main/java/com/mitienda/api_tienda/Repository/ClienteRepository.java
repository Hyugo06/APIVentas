package com.mitienda.api_tienda.Repository;


import com.mitienda.api_tienda.Model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Spring Data JPA entiende este nombre de método y crea la consulta:
    // "SELECT * FROM clientes WHERE dni = ?"
    Optional<Cliente> findByDni(String dni);

    // También podríamos añadir uno para el email
    Optional<Cliente> findByEmail(String email);
}