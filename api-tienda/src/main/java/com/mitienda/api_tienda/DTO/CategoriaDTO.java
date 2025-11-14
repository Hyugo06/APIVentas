package com.mitienda.api_tienda.DTO;

import lombok.Data;

@Data
public class CategoriaDTO {
    private Integer idCategoria;
    private String nombre;
    private Integer idCategoriaPadre; // <-- ¡AÑADE ESTA LÍNEA!
}