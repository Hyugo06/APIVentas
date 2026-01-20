package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.DTO.ClienteRequestDTO; // <--- IMPORTANTE: Importar el DTO
import com.mitienda.api_tienda.Model.Cliente;
import com.mitienda.api_tienda.Service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // GETs (Se mantienen igual)
    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.obtenerTodosLosClientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Integer id) {
        return clienteService.obtenerClientePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Cliente> obtenerPorDni(@PathVariable String dni) {
        return clienteService.obtenerClientePorDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    //  AQUÍ ESTÁ LA CORRECCIÓN (CREAR)
    // ==========================================
    @PostMapping
    public ResponseEntity<?> crearCliente(@Valid @RequestBody ClienteRequestDTO clienteDto) {
        try {
            // 1. Convertir DTO a Entidad MANUALMENTE Y SEGURO
            Cliente cliente = new Cliente();
            cliente.setNombres(clienteDto.getNombres());
            cliente.setApellidos(clienteDto.getApellidos());
            cliente.setCelular(clienteDto.getCelular());

            // ⚠️ PROTECCIÓN ANTI-NULOS:
            // "Si viene DNI, límpialo (trim). Si es null, déjalo null."
            // Esto evita el error: "Cannot invoke trim() on null"
            cliente.setDni(clienteDto.getDni() != null ? clienteDto.getDni().trim() : null);

            // Lo mismo para el Email
            cliente.setEmail(clienteDto.getEmail() != null ? clienteDto.getEmail().trim() : null);

            // 2. Guardar usando el servicio
            Cliente nuevoCliente = clienteService.crearCliente(cliente);
            return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    //  AQUÍ ESTÁ LA CORRECCIÓN (ACTUALIZAR)
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Integer id,
                                               @Valid @RequestBody ClienteRequestDTO clienteDto) {
        try {
            // Convertimos DTO a Entidad para pasarlo al servicio
            Cliente clienteDetalles = new Cliente();
            clienteDetalles.setNombres(clienteDto.getNombres());
            clienteDetalles.setApellidos(clienteDto.getApellidos());
            clienteDetalles.setCelular(clienteDto.getCelular());

            // Protección Anti-Nulos también aquí
            clienteDetalles.setDni(clienteDto.getDni() != null ? clienteDto.getDni().trim() : null);
            clienteDetalles.setEmail(clienteDto.getEmail() != null ? clienteDto.getEmail().trim() : null);

            return clienteService.actualizarCliente(id, clienteDetalles)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE y MOVIMIENTOS (Se mantienen igual)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Integer id) {
        if (clienteService.eliminarCliente(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<com.mitienda.api_tienda.Model.Movimiento>> obtenerHistorial(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerHistorial(id));
    }

    @PostMapping("/{id}/movimientos")
    public ResponseEntity<?> registrarMovimiento(@PathVariable Integer id, @RequestBody com.mitienda.api_tienda.Model.Movimiento movimiento) {
        try {
            com.mitienda.api_tienda.Model.Movimiento nuevoMov = clienteService.registrarMovimiento(id, movimiento);
            return new ResponseEntity<>(nuevoMov, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}