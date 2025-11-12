package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode; // Importante para JSONB
import org.hibernate.type.SqlTypes; // Importante para JSONB
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
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

    @NotEmpty(message = "El codigoSku no puede estar vacío")
    @Column(nullable = false, unique = true)
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

    @Column(name = "precio_compra") // Mapea la columna de la BD
    @Positive(message = "El precio de compra debe ser positivo")
    private BigDecimal precioCompra; // Este es tu nuevo campo de costo

    @NotNull(message = "El stockActual no puede ser nulo")
    @Min(value = 0, message = "El stockActual no puede ser negativo")
    @Column(nullable = false)
    private Integer stockActual;

    // --- Relación con Marcas ---
    // Muchos Productos pueden tener una Marca
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    private Marca marca;

    // ESTO TAMBIÉN ESTÁ BIEN. NO LLEVA @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Para no incluirlo en la API (a menos que un DTO lo pida)
    private List<ImagenProducto> imagenes;

    // --- Manejo del JSONB ---
    // Le decimos a Hibernate que esto es un tipo JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> caracteristicas;
}