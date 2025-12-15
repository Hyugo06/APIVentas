package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.Model.Cupon;
import com.mitienda.api_tienda.Repository.CuponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Service
public class CuponService {

    @Autowired
    private CuponRepository cuponRepository;

    public Cupon aplicarCupon(String codigo, BigDecimal montoCompra) {
        // 1. Buscamos el cupón
        Cupon cupon = cuponRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("El código del cupón no existe."));

        // 2. ¿Está activo el interruptor maestro?
        if (!cupon.isActivo()) {
            throw new RuntimeException("Este cupón ha sido desactivado manualmente.");
        }

        // 3. ¿Ya se venció la fecha?
        if (LocalDate.now().isAfter(cupon.getFechaVencimiento())) {
            throw new RuntimeException("Este cupón ya venció.");
        }

        // 4. ¿Le quedan vidas (usos)?
        if (cupon.getUsosDisponibles() <= 0) {
            throw new RuntimeException("Este cupón se ha agotado.");
        }

        // 5. VALIDACIÓN DE DÍA (Solo si diasPermitidos NO es nulo)
        if (cupon.getDiasPermitidos() != null && !cupon.getDiasPermitidos().isEmpty()) {
            DayOfWeek diaActual = LocalDate.now().getDayOfWeek(); // Ej: MONDAY
            String diaActualString = diaActual.name();

            if (!cupon.getDiasPermitidos().contains(diaActualString)) {
                throw new RuntimeException("Este cupón no es válido hoy (" + diaActualString + ").");
            }
        }

        // 6. VALIDACIÓN DE HORA (Happy Hour)
        if (cupon.getHoraInicio() != null && cupon.getHoraFin() != null) {
            LocalTime horaActual = LocalTime.now();
            if (horaActual.isBefore(cupon.getHoraInicio()) || horaActual.isAfter(cupon.getHoraFin())) {
                throw new RuntimeException("Este cupón solo es válido entre " + cupon.getHoraInicio() + " y " + cupon.getHoraFin());
            }
        }

        // 7. VALIDACIÓN DE MONTO MÍNIMO
        // Aquí podrías parametrizar el "100" si quisieras que cada cupón tenga su propio mínimo
        if (montoCompra.compareTo(new BigDecimal("100")) < 0) {
            throw new RuntimeException("El monto mínimo de compra para este cupón es de S/ 100.00");
        }

        return cupon;
    }
}