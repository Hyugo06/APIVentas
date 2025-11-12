package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.DTO.VentaRequestDTO;
import com.mitienda.api_tienda.Model.Venta;
import com.mitienda.api_tienda.Service.VentaService; // <-- ¡Importa el servicio!
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // <-- ¡Importa @RestController!

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController // <-- ¡¡Debe ser @RestController!!
@RequestMapping("/api/ventas")
public class VentaController {

    // El Controlador SOLO inyecta el SERVICIO
    @Autowired
    private VentaService ventaService;

    /**
     * POST /api/ventas
     * Crea una nueva venta.
     */
    @PostMapping
    public ResponseEntity<?> crearVenta(@Valid @RequestBody VentaRequestDTO ventaRequest) {
        // ^
        // |
        // ¡¡AQUÍ ESTÁ LA SOLUCIÓN!!
        try {
            Venta nuevaVenta = ventaService.crearVenta(ventaRequest);
            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/ventas
     * Obtiene todas las ventas
     */
    @GetMapping
    public List<Venta> obtenerTodas() {
        // Llama al servicio
        return ventaService.obtenerTodasLasVentas();
    }

    /**
     * GET /api/ventas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Integer id) {
        return ventaService.obtenerVentaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/ventas/por-vendedor/{idUsuario}
     */
    @GetMapping("/por-vendedor/{idUsuario}")
    public List<Venta> obtenerVentasPorVendedor(@PathVariable Integer idUsuario) {
        return ventaService.obtenerVentasPorUsuario(idUsuario);
    }

    /**
     * GET /api/ventas/por-cliente/{idCliente}
     */
    @GetMapping("/por-cliente/{idCliente}")
    public List<Venta> obtenerVentasPorCliente(@PathVariable Integer idCliente) {
        return ventaService.obtenerVentasPorCliente(idCliente);
    }

    /**
     * GET /api/ventas/por-fecha
     */
    @GetMapping("/por-fecha")
    public List<Venta> obtenerVentasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ventaService.obtenerVentasPorRangoDeFechas(inicio, fin);
    }

    /**
     * GET /api/ventas/ingresos-totales
     */
    @GetMapping("/ingresos-totales")
    public ResponseEntity<BigDecimal> obtenerIngresosTotales() {
        return ResponseEntity.ok(ventaService.obtenerIngresosTotales());
    }
}