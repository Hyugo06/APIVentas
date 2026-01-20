package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.Model.Cliente;
import com.mitienda.api_tienda.Model.Movimiento;
import com.mitienda.api_tienda.Repository.ClienteRepository;
import com.mitienda.api_tienda.Repository.MovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MovimientoRepository movimientoRepository; // <--- Inyectamos el nuevo repo

    // 1. OBTENER TODOS (Calculando deuda)
    public List<Cliente> obtenerTodosLosClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        // Calculamos la deuda para cada cliente
        for (Cliente c : clientes) {
            c.setDeudaActual(calcularDeudaCliente(c.getIdCliente()));
        }
        return clientes;
    }

    // 2. OBTENER POR ID (Calculando deuda)
    public Optional<Cliente> obtenerClientePorId(Integer id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        cliente.ifPresent(c -> c.setDeudaActual(calcularDeudaCliente(id)));
        return cliente;
    }

    // --- MÉTODOS DE TUS SNIPPETS (Crear, DNI, Update, Delete) SE MANTIENEN IGUAL ---
    public Optional<Cliente> obtenerClientePorDni(String dni) { return clienteRepository.findTopByDni(dni); }

    public Cliente crearCliente(Cliente cliente) {
        if (clienteRepository.findTopByDni(cliente.getDni()).isPresent()) {
            throw new RuntimeException("El DNI " + cliente.getDni() + " ya está registrado.");
        }
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> actualizarCliente(Integer id, Cliente clienteDetalles) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) return Optional.empty();

        Cliente clienteExistente = clienteOpt.get();
        if (!clienteExistente.getDni().equals(clienteDetalles.getDni())) {
            if (clienteRepository.findTopByDni(clienteDetalles.getDni()).isPresent()) {
                throw new RuntimeException("DNI ya existe.");
            }
        }
        clienteExistente.setNombres(clienteDetalles.getNombres());
        clienteExistente.setApellidos(clienteDetalles.getApellidos());
        clienteExistente.setDni(clienteDetalles.getDni());
        clienteExistente.setCelular(clienteDetalles.getCelular());
        clienteExistente.setEmail(clienteDetalles.getEmail());

        return Optional.of(clienteRepository.save(clienteExistente));
    }

    public boolean eliminarCliente(Integer id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
    // -------------------------------------------------------------------------


    // --- NUEVAS FUNCIONES PARA CUENTA CORRIENTE ---

    // A. Función Privada Matemática
    private Double calcularDeudaCliente(Integer idCliente) {
        List<Movimiento> movimientos = movimientoRepository.findByCliente_IdCliente(idCliente);
        double deuda = 0.0;
        for (Movimiento m : movimientos) {
            if ("DEUDA".equals(m.getTipo())) {
                deuda += m.getMonto();
            } else if ("PAGO".equals(m.getTipo())) {
                deuda -= m.getMonto();
            }
        }
        return deuda;
    }

    // B. Obtener Historial
    public List<Movimiento> obtenerHistorial(Integer idCliente) {
        return movimientoRepository.findByCliente_IdCliente(idCliente);
    }

    // C. Registrar Movimiento (Guardar)
    public Movimiento registrarMovimiento(Integer idCliente, Movimiento movimiento) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        movimiento.setCliente(cliente);
        return movimientoRepository.save(movimiento);
    }
}