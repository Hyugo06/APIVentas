package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter // <-- Añade
@Setter // <-- Añade
@NoArgsConstructor // <-- Añade
@Entity
@Table(name = "detalle_venta")
@JsonIgnoreProperties({"hibernateLazyInitializer"}) // <-- ¡AÑADE ESTO!
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDetalle;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private BigDecimal precioUnitario; // Precio al momento de la venta

    @Column(nullable = false)
    private BigDecimal subtotal;

    // --- Relaciones ---

    // Muchos detalles pertenecen a UNA Venta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    @JsonIgnore // ¡¡CRUCIAL!! Para romper el bucle Venta -> Detalle -> Venta
    private Venta venta;

    // Muchos detalles apuntan a UN Producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}