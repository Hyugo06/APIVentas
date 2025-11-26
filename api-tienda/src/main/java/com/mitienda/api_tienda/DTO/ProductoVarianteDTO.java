package com.mitienda.api_tienda.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ProductoVarianteDTO {
    private Integer idVariante;
    private String color;
    private String talla;
    private String skuVariante;
    private Integer stockActual;
    private String urlImagen;
    private List<String> galeriaImagenes;
}