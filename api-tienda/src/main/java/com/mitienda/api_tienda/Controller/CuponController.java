package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.Model.Cupon;
import com.mitienda.api_tienda.Service.CuponService;
import com.mitienda.api_tienda.Repository.CuponRepository; // Lo usamos para guardar rápido
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cupones")
public class CuponController {

    @Autowired
    private CuponService cuponService;

    @Autowired
    private CuponRepository cuponRepository;

    // 1. ENDPOINT PARA VALIDAR (Usado en el Checkout)
    // Recibe un JSON: { "codigo": "VERANO20", "monto": 150.00 }
    @PostMapping("/validar")
    public ResponseEntity<?> validarCupom(@RequestBody Map<String, Object> request) {
        try {
            String codigo = (String) request.get("codigo");
            // Convertimos el monto que viene del JSON a BigDecimal
            Double montoDouble = Double.valueOf(request.get("monto").toString());
            BigDecimal montoCompra = BigDecimal.valueOf(montoDouble);

            Cupon cuponValido = cuponService.aplicarCupon(codigo, montoCompra);

            return ResponseEntity.ok(cuponValido);

        } catch (RuntimeException e) {
            // Si el servicio lanza un error (vencido, agotado, etc.), devolvemos 400 Bad Request
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    // 2. ENDPOINT PARA CREAR (Usado por el Admin)
    // Aquí deberías agregar seguridad para que solo el ADMIN pueda entrar
    @PostMapping
    public ResponseEntity<?> crearCupom(@RequestBody Cupon cupon) {
        try {
            // Guardamos directamente (podrías agregar validaciones extra si quieres)
            Cupon nuevoCupon = cuponRepository.save(cupon);
            return ResponseEntity.ok(nuevoCupon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el cupón: " + e.getMessage());
        }
    }

    // 3. LISTAR TODOS (Para que el Admin los vea)
    @GetMapping
    public ResponseEntity<?> listarCupones() {
        return ResponseEntity.ok(cuponRepository.findAll());
    }

    // OBTENER POR ID (Para cargar el formulario de edición)
    @GetMapping("/{id}")
    public ResponseEntity<Cupon> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(cuponService.obtenerPorId(id));
    }

    // ACTUALIZAR
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Cupon cupon) {
        return ResponseEntity.ok(cuponService.actualizar(id, cupon));
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        cuponService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}