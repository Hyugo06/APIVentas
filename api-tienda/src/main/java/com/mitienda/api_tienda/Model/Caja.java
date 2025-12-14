package com.mitienda.api_tienda.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cajas")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCaja;

    // Usuario que abrió la caja (El responsable)
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    // Con cuánto dinero iniciamos (sencillo)
    @Column(nullable = false)
    private BigDecimal montoInicial;

    // Cuánto dinero calculó el sistema que debería haber (Ventas + Inicial)
    private BigDecimal montoSistema;

    // Cuánto dinero contó el usuario físicamente al cerrar
    private BigDecimal montoReal;

    // La diferencia (Si falta dinero, saldrá negativo)
    private BigDecimal diferencia;

    // Estado: "ABIERTA" o "CERRADA"
    @Column(nullable = false, length = 20)
    private String estado;

    @PrePersist
    public void prePersist() {
        this.fechaApertura = LocalDateTime.now();
        this.estado = "ABIERTA";
    }
}