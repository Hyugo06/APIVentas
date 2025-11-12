package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "imagenes_producto")
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idImagen;

    @Column(nullable = false, name = "url_imagen")
    private String urlImagen;

    @Column(name = "descripcion_alt")
    private String descripcionAlt;

    private Integer orden;

    // --- Relación Inversa ---
    // Muchas imágenes pertenecen a UN Producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonIgnore // ¡¡Para evitar bucles infinitos!!
    private Producto producto;
}