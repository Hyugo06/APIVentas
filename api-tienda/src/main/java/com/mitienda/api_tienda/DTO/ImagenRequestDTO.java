package com.mitienda.api_tienda.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImagenRequestDTO {

    @NotEmpty(message = "La URL de la imagen es obligatoria")
    private String urlImagen;

    private String descripcionAlt;

    @NotNull(message = "El orden es obligatorio")
    private Integer orden;
}