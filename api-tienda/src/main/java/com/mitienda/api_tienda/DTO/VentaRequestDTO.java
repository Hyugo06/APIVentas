package com.mitienda.api_tienda.DTO;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class VentaRequestDTO {

    // --- ¡ELIMINADO! ---
    // private Integer idCliente;
    // private Integer idUsuario;

    // --- ¡AÑADIDO! ---
    // Recibimos el objeto completo con los datos del formulario.
    @NotNull(message = "Los datos del cliente no pueden ser nulos")
    @Valid // <-- Le dice a Spring que valide los campos DENTRO de este objeto
    private ClienteRequestDTO clienteData;

    @NotEmpty(message = "El tipoComprobante no puede estar vacío")
    private String tipoComprobante;

    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "La lista de detalles no puede estar vacía")
    @Valid
    private List<DetalleVentaDTO> detalles;

    // (Tu clase anidada DetalleVentaDTO se queda igual)
    @Data
    public static class DetalleVentaDTO {
        @NotNull
        private Integer idProducto;
        @NotNull
        @Min(value = 1)
        private Integer cantidad;
    }
}