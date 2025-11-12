package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- IMPORTANTE
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "categorias")
@JsonIgnoreProperties({"hibernateLazyInitializer"}) // <-- ¡AÑADE ESTA LÍNEA!
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoria;

    private String nombre;
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre")
    private Categoria categoriaPadre;

    // Ignora la lista de hijos
    @OneToMany(mappedBy = "categoriaPadre")
    @JsonIgnore
    private List<Categoria> subcategorias;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Producto> productos;
}