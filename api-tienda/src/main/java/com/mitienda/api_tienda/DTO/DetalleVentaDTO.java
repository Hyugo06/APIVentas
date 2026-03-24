package com.mitienda.api_tienda.DTO;

import lombok.Data;

@Data
public class DetalleVentaDTO {
    private Integer idProducto;
    private Integer idVariante; // Para saber qué talla/color es
    private Integer cantidad;
    private Integer idSucursal; // NUEVO: Para saber de qué tienda descontar
}