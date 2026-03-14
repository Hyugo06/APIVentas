package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode; // Importante para JSONB
import org.hibernate.type.SqlTypes; // Importante para JSONB
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "productos")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(name = "precio_regular")
    @Positive(message = "El precio regular debe ser positivo")
    private BigDecimal precioRegular;


    @Column(name = "codigo_sku")
    private String codigoSku;

    @NotEmpty(message = "El nombre no puede estar vacío")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precioVenta no puede ser nulo")
    @Positive(message = "El precioVenta debe ser un número positivo")
    @Column(nullable = false)
    private BigDecimal precioVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal")
    private Sucursal sucursal;

    @Column(name = "precio_compra") // Mapea la columna de la BD
    @Positive(message = "El precio de compra debe ser positivo")
    private BigDecimal precioCompra; // Este es tu nuevo campo de costo

    @NotNull(message = "El stockActual no puede ser nulo")
    @Min(value = 0, message = "El stockActual no puede ser negativo")
    @Column(nullable = false)
    private Integer stockActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    @ToString.Exclude // <-- MANTENER
    private Marca marca;

    @Column(name = "en_oferta")
    private Boolean enOferta = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    @ToString.Exclude // <-- MANTENER
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude // <-- MANTENER
    private List<ImagenProducto> imagenes;

    // --- Manejo del JSONB ---
    // Le decimos a Hibernate que esto es un tipo JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> caracteristicas;

    @Column(name = "url_imagen")
    private String urlImagen;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoVariante> variantes;

    @PrePersist
    @PreUpdate
    private void recalcularStockTotal() {
        // 1. Empezamos con el contador en cero
        int total = 0;

        // 2. Verificamos si hay variantes en la lista
        if (this.variantes != null && !this.variantes.isEmpty()) {

            // 3. Recorremos cada variante una por una
            for (ProductoVariante variante : this.variantes) {
                // Sumamos el stock de la variante (evitando nulos por seguridad)
                if (variante.getStockActual() != null) {
                    total += variante.getStockActual();
                }
            }

            // 4. Actualizamos el campo principal con la suma total
            this.stockActual = total;
        }
    }
}


