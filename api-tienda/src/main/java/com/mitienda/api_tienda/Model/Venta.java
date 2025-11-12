package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
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

    // Este campo será calculado por el TRIGGER de la BD
    @Column(nullable = false)
    private BigDecimal montoTotal = BigDecimal.ZERO;

    // --- Relaciones ---

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