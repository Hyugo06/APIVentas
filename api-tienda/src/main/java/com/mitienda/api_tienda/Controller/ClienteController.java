package com.mitienda.api_tienda.Controller;


import com.mitienda.api_tienda.Model.Cliente;
import com.mitienda.api_tienda.Service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes") // URL base para esta entidad
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // ENDPOINT: GET /api/clientes
    // Obtiene todos los clientes
    @GetMapping
    public List<Cliente> obtenerTodos() {
        return clienteService.obtenerTodosLosClientes();
    }

    // ENDPOINT: GET /api/clientes/{id}
    // Obtiene un cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Integer id) {
        return clienteService.obtenerClientePorId(id)
                .map(ResponseEntity::ok) // Si lo encuentra, devuelve 200 OK
                .orElse(ResponseEntity.notFound().build()); // Si no, devuelve 404
    }

    // ENDPOINT: GET /api/clientes/dni/{dni}
    // Obtiene un cliente por DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<Cliente> obtenerPorDni(@PathVariable String dni) {
        return clienteService.obtenerClientePorDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ENDPOINT: POST /api/clientes
    // Crea un nuevo cliente
    @PostMapping
    public ResponseEntity<?> crearCliente(@Valid @RequestBody Cliente cliente) { // <-- ¡AÑADIDO!
        try {
            Cliente nuevoCliente = clienteService.crearCliente(cliente);
            return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ENDPOINT: PUT /api/clientes/{id}
    // Actualiza un cliente existente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Integer id,
                                               @Valid @RequestBody Cliente clienteDetalles) { // <-- ¡AÑADIDO!
        try {
            return clienteService.actualizarCliente(id, clienteDetalles)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ENDPOINT: DELETE /api/clientes/{id}
    // Elimina un cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Integer id) {
        if (clienteService.eliminarCliente(id)) {
            return ResponseEntity.noContent().build(); // Devuelve 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // Devuelve 404
        }
    }
}