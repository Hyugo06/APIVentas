package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMovimiento;

    @Column(nullable = false)
    private String tipo; // "DEUDA" o "PAGO"

    @Column(nullable = false)
    private Double monto;

    private String comentario;  // Concepto (ej: "Zapatillas", "Abono Yape")
    private String comprobante; // Opcional (ej: "OP-12345")

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    // Relación: Muchos movimientos pertenecen a un Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonIgnore // Evita bucles infinitos al convertir a JSON
    private Cliente cliente;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now(); // Se guarda la fecha actual automáticamente
    }
}