package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.Service.CajaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/caja")
public class CajaController {

    @Autowired
    private CajaService cajaService;

    // POST /api/caja/abrir
    // Body: { "montoInicial": 50.00 }
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody Map<String, BigDecimal> body, Principal principal) {
        try {
            BigDecimal monto = body.get("montoInicial");
            return ResponseEntity.ok(cajaService.abrirCaja(principal.getName(), monto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/caja/estado
    @GetMapping("/estado")
    public ResponseEntity<?> obtenerEstado(Principal principal) {
        try {
            return ResponseEntity.ok(cajaService.obtenerEstadoCaja(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/caja/cerrar
    // Body: { "montoReal": 520.50 }
    @PostMapping("/cerrar")
    public ResponseEntity<?> cerrarCaja(@RequestBody Map<String, BigDecimal> body, Principal principal) {
        try {
            BigDecimal montoReal = body.get("montoReal");
            return ResponseEntity.ok(cajaService.cerrarCaja(principal.getName(), montoReal));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}