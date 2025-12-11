package com.mitienda.api_tienda.Model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

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

    @Column(name = "codigo_corto", length = 10, nullable = true)
    private String codigoCorto;
    // ---------------------------------

    private String descripcion;
}