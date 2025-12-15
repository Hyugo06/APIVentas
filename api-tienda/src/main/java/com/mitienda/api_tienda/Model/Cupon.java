package com.mitienda.api_tienda.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime; // <--- Importante para las horas

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cupones")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCupom;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String tipoDescuento; // "PORCENTAJE" o "FIJO"

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private Integer usosDisponibles; // Contador global (ej: 50 usos totales)

    private boolean activo = true;

    // --- NUEVOS CAMPOS DE RESTRICCIÓN ---

    // Rango de horas (Ej: De 14:00 a 18:00)
    // Si son nulos, significa que vale todo el día.
    private LocalTime horaInicio;
    private LocalTime horaFin;

    // Días permitidos (Ej: "MONDAY,WEDNESDAY")
    // Si es nulo, vale todos los días.
    private String diasPermitidos;
}