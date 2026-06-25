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
    private MovimientoRepository movimientoRepository;

    public List<Cliente> obtenerTodosLosClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        for (Cliente c : clientes) {
            c.setDeudaActual(calcularDeudaCliente(c.getIdCliente()));
        }
        return clientes;
    }

    public Optional<Cliente> obtenerClientePorId(Integer id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        cliente.ifPresent(c -> c.setDeudaActual(calcularDeudaCliente(id)));
        return cliente;
    }

    public Optional<Cliente> obtenerClientePorDni(String dni) { return clienteRepository.findTopByDni(dni); }


    public Optional<Cliente> actualizarCliente(Integer id, Cliente clienteDetalles) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isEmpty()) return Optional.empty();

        Cliente clienteExistente = clienteOpt.get();


        String nuevoDni = clienteDetalles.getDni();
        if (nuevoDni != null && nuevoDni.trim().isEmpty()) {
            nuevoDni = null;
        }

        if (nuevoDni == null && clienteExistente.getDni() != null && !clienteExistente.getDni().startsWith("00")) {
            Optional<String> ultimoDniSecuencial = clienteRepository.findLastSecuencialDni();
            int siguienteNumero = 1;
            if (ultimoDniSecuencial.isPresent()) {
                siguienteNumero = Integer.parseInt(ultimoDniSecuencial.get()) + 1;
            }
            nuevoDni = String.format("%08d", siguienteNumero);
        }

        if (nuevoDni != null && !nuevoDni.equals(clienteExistente.getDni())) {
            if (clienteRepository.findTopByDni(nuevoDni).isPresent()) {
                throw new RuntimeException("CLI-002|El DNI ya pertenece a otro cliente.");
            }
        }

        String nuevoCelular = clienteDetalles.getCelular();
        if (nuevoCelular != null && nuevoCelular.trim().isEmpty()) {
            nuevoCelular = null;
        }
        if (nuevoCelular == null && clienteExistente.getCelular() != null && !clienteExistente.getCelular().startsWith("00")) {
            Optional<String> ultimoCelularSecuencial = clienteRepository.findLastSecuencialCelular();
            int siguienteNumeroCel = 1;
            if (ultimoCelularSecuencial.isPresent()) {
                siguienteNumeroCel = Integer.parseInt(ultimoCelularSecuencial.get()) + 1;
            }
            nuevoCelular = String.format("%09d", siguienteNumeroCel);
        }

        String nuevoEmail = clienteDetalles.getEmail();
        if (nuevoEmail != null && nuevoEmail.trim().isEmpty()) {
            nuevoEmail = null;
        }
        if (nuevoEmail != null && !nuevoEmail.equals(clienteExistente.getEmail())) {
            if (clienteRepository.findByEmail(nuevoEmail).isPresent()) {
                throw new RuntimeException("CLI-003|El correo ya existe.");
            }
        }

        clienteExistente.setNombres(clienteDetalles.getNombres());
        clienteExistente.setApellidos(clienteDetalles.getApellidos());
        clienteExistente.setDni(nuevoDni);
        clienteExistente.setCelular(nuevoCelular);
        clienteExistente.setEmail(nuevoEmail);

        return Optional.of(clienteRepository.save(clienteExistente));
    }

    public boolean eliminarCliente(Integer id) {
        if (clienteRepository.existsById(id)) {
            // 🌟 1. Calcular la deuda antes de permitir borrar
            Double deuda = calcularDeudaCliente(id);
            if (deuda != 0.0) {
                throw new RuntimeException("No se puede eliminar un cliente que mantiene una deuda activa de S/ " + deuda);
            }

            // 🌟 2. Borrado en Cascada: Limpiamos primero sus movimientos para no romper la base de datos
            List<Movimiento> movimientos = movimientoRepository.findByCliente_IdCliente(id);
            if (!movimientos.isEmpty()) {
                movimientoRepository.deleteAll(movimientos);
            }

            // 🌟 3. Finalmente eliminamos al cliente
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Cliente crearCliente(Cliente cliente) {

        if (cliente.getDni() != null && cliente.getDni().trim().isEmpty()) {
            cliente.setDni(null);
        }
        if (cliente.getEmail() != null && cliente.getEmail().trim().isEmpty()) {
            cliente.setEmail(null);
        }
        if (cliente.getCelular() != null && cliente.getCelular().trim().isEmpty()) {
            cliente.setCelular(null);
        }

        if (cliente.getDni() == null) {
            Optional<String> ultimoDniSecuencial = clienteRepository.findLastSecuencialDni();
            int siguienteNumero = 1;
            if (ultimoDniSecuencial.isPresent()) {
                siguienteNumero = Integer.parseInt(ultimoDniSecuencial.get()) + 1;
            }
            String nuevoDniSecuencial = String.format("%08d", siguienteNumero);
            cliente.setDni(nuevoDniSecuencial);
        }

        if (cliente.getCelular() == null) {
            Optional<String> ultimoCelularSecuencial = clienteRepository.findLastSecuencialCelular();
            int siguienteNumeroCel = 1;
            if (ultimoCelularSecuencial.isPresent()) {
                siguienteNumeroCel = Integer.parseInt(ultimoCelularSecuencial.get()) + 1;
            }
            String nuevoCelularSecuencial = String.format("%09d", siguienteNumeroCel);
            cliente.setCelular(nuevoCelularSecuencial);
        }


        String apellidoBusqueda = cliente.getApellidos() != null ? cliente.getApellidos().trim() : "";
        List<Cliente> homonimos = clienteRepository.findByNombresIgnoreCaseAndApellidosIgnoreCase(cliente.getNombres().trim(), apellidoBusqueda);

        if (!homonimos.isEmpty()) {
            throw new RuntimeException("CLI-001|Ya existe un cliente registrado como '" + cliente.getNombres() + " " + apellidoBusqueda + "'. Verifique usando el DNI si es la misma persona.");
        }

        if (cliente.getDni() != null) {
            if (clienteRepository.findTopByDni(cliente.getDni()).isPresent()) {
                throw new RuntimeException("CLI-002|El DNI/RUC " + cliente.getDni() + " ya pertenece a otro cliente.");
            }
        }

        if (cliente.getEmail() != null) {
            if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
                throw new RuntimeException("CLI-003|El correo electrónico " + cliente.getEmail() + " ya está en uso.");
            }
        }

        return clienteRepository.save(cliente);
    }

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