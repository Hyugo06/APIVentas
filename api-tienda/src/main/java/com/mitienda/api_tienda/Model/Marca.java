package com.mitienda.api_tienda.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data; // Si usas Lombok

import java.util.List;

@Data
@Entity
@Table(name = "marcas")
@JsonIgnoreProperties({"hibernateLazyInitializer"}) // <-- ¡AÑADE ESTA LÍNEA!
public class Marca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMarca;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String descripcion;

    // ¡Importante! Si un producto referencia una marca,
    // necesitamos esta lista para que JPA entienda la relación
    @OneToMany(mappedBy = "marca", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Producto> productos;
}