package com.mitienda.api_tienda.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductoPublicoDTO {
    private Integer idProducto;
    private String codigoSku;
    private String nombre;
    private String descripcion;
    private BigDecimal precioRegular;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private MarcaDTO marca;
    private CategoriaDTO categoria;
    private Map<String, Object> caracteristicas;
}