package com.mitienda.api_tienda.DTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL; // Para validar que es una URL

@Data
public class ImagenRequestDTO {

    @NotEmpty(message = "La urlImagen no puede estar vacía")
    @URL(message = "Debe ser una URL válida (ej. http://... o https://...)")
    private String urlImagen;

    private String descripcionAlt;

    private Integer orden = 1; // Valor por defecto
}