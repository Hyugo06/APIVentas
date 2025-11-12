package com.mitienda.api_tienda.Service;


import com.mitienda.api_tienda.Model.Cliente;
import com.mitienda.api_tienda.Repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Obtener todos los clientes
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    // Obtener un cliente por su ID
    public Optional<Cliente> obtenerClientePorId(Integer id) {
        return clienteRepository.findById(id);
    }

    // Obtener un cliente por su DNI
    public Optional<Cliente> obtenerClientePorDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    // Crear un nuevo cliente con validaciones
    public Cliente crearCliente(Cliente cliente) {
        // Validación 1: Asegurarnos que el DNI no exista
        if (clienteRepository.findByDni(cliente.getDni()).isPresent()) {
            // Es mejor lanzar una excepción personalizada, pero esto funciona
            throw new RuntimeException("El DNI " + cliente.getDni() + " ya está registrado.");
        }

        // Validación 2: Asegurarnos que el Email no exista (si se provee)
        if (cliente.getEmail() != null && clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new RuntimeException("El email " + cliente.getEmail() + " ya está registrado.");
        }

        return clienteRepository.save(cliente);
    }

    // Actualizar un cliente existente
    public Optional<Cliente> actualizarCliente(Integer id, Cliente clienteDetalles) {
        // 1. Buscar el cliente
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return Optional.empty(); // No se encontró, devuelve vacío
        }

        Cliente clienteExistente = clienteOpt.get();

        // 2. Validar DNI (si cambió y si pertenece a OTRO cliente)
        if (!clienteExistente.getDni().equals(clienteDetalles.getDni())) {
            if (clienteRepository.findByDni(clienteDetalles.getDni()).isPresent()) {
                throw new RuntimeException("El nuevo DNI " + clienteDetalles.getDni() + " ya pertenece a otro cliente.");
            }
        }
        // (Aquí iría una validación similar para el email)

        // 3. Actualizar los datos
        clienteExistente.setNombres(clienteDetalles.getNombres());
        clienteExistente.setApellidos(clienteDetalles.getApellidos());
        clienteExistente.setDni(clienteDetalles.getDni());
        clienteExistente.setCelular(clienteDetalles.getCelular());
        clienteExistente.setEmail(clienteDetalles.getEmail());

        // 4. Guardar y devolver
        return Optional.of(clienteRepository.save(clienteExistente));
    }

    // Eliminar un cliente
    public boolean eliminarCliente(Integer id) {
        if (clienteRepository.existsById(id)) {
            // Ojo: Esto fallará si el cliente tiene ventas asociadas (por la FK)
            // En un caso real, quizás solo querrías "desactivar" al cliente.
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}