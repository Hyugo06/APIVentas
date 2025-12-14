package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.Model.Caja;
import com.mitienda.api_tienda.Model.Usuario;
import com.mitienda.api_tienda.Repository.CajaRepository;
import com.mitienda.api_tienda.Repository.UsuarioRepository;
import com.mitienda.api_tienda.Repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class CajaService {

    @Autowired
    private CajaRepository cajaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private VentaRepository ventaRepository;

    // --- 1. ABRIR CAJA ---
    public Caja abrirCaja(String username, BigDecimal montoInicial) {
        Usuario usuario = (Usuario) usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si ya tiene una abierta
        Optional<Caja> cajaAbierta = cajaRepository.findByUsuarioAndEstado(usuario, "ABIERTA");
        if (cajaAbierta.isPresent()) {
            throw new RuntimeException("Ya tienes una caja abierta. Debes cerrarla primero.");
        }

        Caja nuevaCaja = new Caja();
        nuevaCaja.setUsuario(usuario);
        nuevaCaja.setMontoInicial(montoInicial);
        // El @PrePersist del modelo pondrá fechaApertura y estado "ABIERTA" automáticamente

        return cajaRepository.save(nuevaCaja);
    }

    // --- 2. CONSULTAR ESTADO (Saber si tengo caja abierta y cuánto llevo vendido) ---
    public Map<String, Object> obtenerEstadoCaja(String username) {
        Usuario usuario = (Usuario) usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Caja caja = cajaRepository.findByUsuarioAndEstado(usuario, "ABIERTA")
                .orElse(null);

        if (caja == null) {
            return Map.of("estado", "CERRADA");
        }

        // Si está abierta, calculamos cuánto lleva vendido hasta este segundo
        BigDecimal ventasDelDia = ventaRepository.sumarVentasDelUsuarioDesde(usuario.getIdUsuario(), caja.getFechaApertura());
        BigDecimal totalEsperado = caja.getMontoInicial().add(ventasDelDia);

        return Map.of(
                "estado", "ABIERTA",
                "idCaja", caja.getIdCaja(),
                "fechaApertura", caja.getFechaApertura(),
                "montoInicial", caja.getMontoInicial(),
                "ventasActuales", ventasDelDia,
                "totalEsperado", totalEsperado
        );
    }

    // --- 3. CERRAR CAJA (Arqueo Final) ---
    @Transactional
    public Caja cerrarCaja(String username, BigDecimal montoRealEnMano) {
        Usuario usuario = (Usuario) usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Caja caja = cajaRepository.findByUsuarioAndEstado(usuario, "ABIERTA")
                .orElseThrow(() -> new RuntimeException("No tienes ninguna caja abierta para cerrar."));

        // 1. Calculamos cuánto DEBERÍA haber
        BigDecimal ventasTotales = ventaRepository.sumarVentasDelUsuarioDesde(usuario.getIdUsuario(), caja.getFechaApertura());
        BigDecimal montoSistema = caja.getMontoInicial().add(ventasTotales);

        // 2. Calculamos diferencia (Real - Sistema)
        // Ej: Real 100 - Sistema 100 = 0 (Perfecto)
        // Ej: Real 90 - Sistema 100 = -10 (Falta dinero)
        // Ej: Real 110 - Sistema 100 = +10 (Sobra dinero)
        BigDecimal diferencia = montoRealEnMano.subtract(montoSistema);

        // 3. Guardamos cierre
        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoSistema(montoSistema);
        caja.setMontoReal(montoRealEnMano);
        caja.setDiferencia(diferencia);
        caja.setEstado("CERRADA");

        return cajaRepository.save(caja);
    }
}