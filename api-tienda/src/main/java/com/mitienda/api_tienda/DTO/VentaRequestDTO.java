package com.mitienda.api_tienda.DTO;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class VentaRequestDTO {

    private Integer idCliente; // <-- ¡¡ESTA LÍNEA ES LA QUE TE FALTA!!

    @NotNull(message = "El idUsuario (vendedor) no puede ser nulo")
    private Integer idUsuario;

    @NotEmpty(message = "El tipoComprobante no puede estar vacío")
    private String tipoComprobante;

    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "La lista de detalles no puede estar vacía")
    @Valid
    private List<DetalleVentaDTO> detalles;

    // Clase anidada
    @Data
    public static class DetalleVentaDTO {

        @NotNull(message = "El idProducto no puede ser nulo")
        private Integer idProducto;

        @NotNull(message = "La cantidad no puede ser nula")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;
    }


}