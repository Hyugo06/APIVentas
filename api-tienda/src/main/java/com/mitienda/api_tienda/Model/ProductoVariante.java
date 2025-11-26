package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "producto_variantes")
public class ProductoVariante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVariante;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String talla;

    @Column(name = "sku_variante", length = 100)
    private String skuVariante;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "url_imagen")
    private String urlImagen;

    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenProducto> imagenes;

    // Relación con el Padre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @JsonIgnore // Evita bucle infinito al serializar
    private Producto producto;
}