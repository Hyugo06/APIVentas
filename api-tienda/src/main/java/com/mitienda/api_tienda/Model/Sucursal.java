package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sucursales")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSucursal;

    @Column(nullable = false, unique = true)
    private String nombre; // Ejemplo: "Ropa", "Hogar", "Almacén"

    // Constructores
    public Sucursal() {}

    public Sucursal(String nombre) {
        this.nombre = nombre;
    }

    // Getters y Setters
    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}