package com.mitienda.api_tienda.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
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
    private String urlImagen;

    private MarcaDTO marca;
    private CategoriaDTO categoria;
    private Map<String, Object> caracteristicas;

    private List<ProductoVarianteDTO> variantes;
    private Boolean enOferta;
    private java.util.Map<String, Integer> sucursal;
}