package com.mitienda.api_tienda.DTO;

import lombok.Data;

@Data
public class ImagenDTO {
    private Integer idImagen;
    private String urlImagen;
    private String descripcionAlt;
    private Integer orden;
}