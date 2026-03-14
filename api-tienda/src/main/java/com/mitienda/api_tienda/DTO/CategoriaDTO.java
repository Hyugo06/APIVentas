package com.mitienda.api_tienda.DTO;

import lombok.Data;

@Data
public class CategoriaDTO {
    private Integer idCategoria;
    private String nombre;
    private String codigoCorto;
    private String descripcion;
    private Integer idCategoriaPadre;
    private String rutaCompleta;
    private CategoriaDTO categoriaPadre;
}