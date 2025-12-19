package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
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

    @Column(name = "sku_variante")
    private String skuVariante;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "url_imagen")
    private String urlImagen;

    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ImagenProducto> imagenes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    @JsonIgnore // <--- ¡AGREGA ESTO! Rompe el bucle Producto <-> Variante
    private Producto producto;

    public void setImagenes(List<ImagenProducto> imagenes) {
        this.imagenes = imagenes;
        // Vinculamos la imagen con esta variante
        for (ImagenProducto img : imagenes) {
            img.setVariante(this);
        }
    }
}
