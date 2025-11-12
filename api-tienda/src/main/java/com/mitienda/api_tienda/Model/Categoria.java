package com.mitienda.api_tienda.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString; // (Este sí lo dejamos por categoriaPadre)
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
// ¡Ya no importamos List!

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categorias")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoria;

    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;

    // --- La relación 'hacia arriba' está bien ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_padre")
    @ToString.Exclude // Dejamos esto por si acaso
    private Categoria categoriaPadre;

    // --- ¡¡BORRA ESTOS DOS BLOQUES!! ---

    // @OneToMany(mappedBy = "categoriaPadre")
    // @JsonIgnore
    // @ToString.Exclude
    // private List<Categoria> subcategorias;

    // @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    // @JsonIgnore
    // @ToString.Exclude
    // private List<Producto> productos;
}