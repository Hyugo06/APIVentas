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
@Data
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

    @Column(name = "id_sucursal")
    private Integer idSucursal;

    // --- Relaciones ---

    // Muchos detalles pertenecen a UNA Venta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta")
    @JsonIgnore // <--- ¡AGREGA ESTO! Rompe el bucle Venta <-> Detalle
    private Venta venta;

    // Muchos detalles apuntan a UN Producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_variante")
    private ProductoVariante variante;
}