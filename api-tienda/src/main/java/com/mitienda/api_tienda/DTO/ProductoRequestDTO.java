package com.mitienda.api_tienda.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductoRequestDTO {


    private String codigoSku;

    @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    @Positive(message = "El precio regular debe ser positivo")
    private BigDecimal precioRegular;

    @NotNull(message = "El precioVenta no puede ser nulo")
    @Positive(message = "El precioVenta debe ser positivo")
    private BigDecimal precioVenta;

    @Positive(message = "El precio de compra debe ser positivo")
    private BigDecimal precioCompra;

    @NotNull(message = "El stockActual no puede ser nulo")
    @Min(value = 0, message = "El stockActual no puede ser negativo")
    private Integer stockActual;

    @NotNull(message = "El idMarca no puede ser nulo")
    private Integer idMarca;

    @NotNull(message = "El idCategoria no puede ser nulo")
    private Integer idCategoria;

    @NotNull(message = "El idSucursal no puede ser nulo")
    private Integer idSucursal;

    private Map<String, Object> caracteristicas;

    private String urlImagen;

    private List<ProductoVarianteDTO> variantes;

    private Boolean enOferta;
}