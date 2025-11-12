package com.mitienda.api_tienda.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
// ¡Ya no necesitamos ToString ni EqualsAndHashCode!
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
// ¡Ya no importamos List!

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "marcas")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMarca;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    // --- ¡¡BORRA TODO ESTE BLOQUE!! ---
    // @OneToMany(mappedBy = "marca", fetch = FetchType.LAZY)
    // @JsonIgnore
    // @ToString.Exclude
    // private List<Producto> productos;
}