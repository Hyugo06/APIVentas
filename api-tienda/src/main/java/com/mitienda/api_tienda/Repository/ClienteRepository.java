package com.mitienda.api_tienda.Repository;

import com.mitienda.api_tienda.Model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List; // 👈 ¡ESTA ES LA IMPORTACIÓN CORRECTA QUE REEMPLAZA A HIBERNATE!
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findTopByDni(String dni);

    Optional<Cliente> findByEmail(String email);

    // 👇 CONSULTAS NATIVAS PARA BUSCAR LA SECUENCIA MÁS ALTA 👇
    @Query(value = "SELECT dni FROM clientes WHERE dni ~ '^[0-9]{8}$' AND dni LIKE '0%' ORDER BY CAST(dni AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastSecuencialDni();

    @Query(value = "SELECT celular FROM clientes WHERE celular ~ '^[0-9]{9}$' AND celular LIKE '0%' ORDER BY CAST(celular AS INTEGER) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastSecuencialCelular();

    // 👇 BUSCAR HOMÓNIMOS (Ignorando mayúsculas y minúsculas) 👇
    List<Cliente> findByNombresIgnoreCaseAndApellidosIgnoreCase(String nombres, String apellidos);
}