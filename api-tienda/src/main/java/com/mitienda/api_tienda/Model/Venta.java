package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter // <-- Añade
@Setter // <-- Añade
@NoArgsConstructor // <-- Añade
@Entity
@Table(name = "ventas")
@JsonIgnoreProperties({"hibernateLazyInitializer"}) // <-- ¡AÑADE ESTO!
public class Venta{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVenta;

    @Column(name = "fecha_venta", updatable = false, insertable = false)
    private LocalDateTime fechaVenta;

    @Column(nullable = false)
    private String tipoComprobante;

    // Relación con el Cupón
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cupom")
    private Cupon cupon;

    // Este campo será calculado por el TRIGGER de la BD
    @Column(name = "monto_descuento")
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    // Modifica esta línea en Venta.java
    @Column(name = "total", nullable = false, columnDefinition = "numeric(38,2) default 0")
    private BigDecimal montoTotal;

    @Column(nullable = true) // <--- Permitimos nulos para no pelear con las ventas viejas
    private String estado;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = "COMPLETADA";
        }
    }

    // Muchas ventas pueden ser de un cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente") // Puede ser nulo
    private Cliente cliente;

    // Muchas ventas son hechas por un usuario (vendedor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // Una Venta tiene muchos Detalles de Venta
    // "cascade = CascadeType.ALL" significa que si borras una Venta, se borran sus detalles.
    // "mappedBy" apunta al campo 'venta' en la clase DetalleVenta
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles;
}