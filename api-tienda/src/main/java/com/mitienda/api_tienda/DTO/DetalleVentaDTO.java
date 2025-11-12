package com.mitienda.api_tienda.DTO;

import lombok.Data;

@Data
public class DetalleVentaDTO { // <-- ¡SOLUCIÓN! Añade 'public' aquí
    private Integer idProducto;
    private Integer cantidad;
}
