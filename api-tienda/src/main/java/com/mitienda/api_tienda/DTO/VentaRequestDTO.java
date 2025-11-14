package com.mitienda.api_tienda.DTO;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class VentaRequestDTO {

    // --- ¡ELIMINADO! ---
    // El idCliente "falso" ya no se necesita.
    // private Integer idCliente;

    // --- ¡ELIMINADO! ---
    // El idUsuario se obtendrá del token de seguridad (Principal)
    // @NotNull(message = "El idUsuario (vendedor) no puede ser nulo")
    // private Integer idUsuario;

    // --- ¡AÑADIDO! ---
    // Ahora recibimos el objeto completo con los datos del formulario.
    @NotNull(message = "Los datos del cliente no pueden ser nulos")
    @Valid // <-- Le dice a Spring que valide los campos DENTRO de este objeto
    private ClienteRequestDTO clienteData;

    @NotEmpty(message = "El tipoComprobante no puede estar vacío")
    private String tipoComprobante;

    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "La lista de detalles no puede estar vacía")
    @Valid
    private List<DetalleVentaDTO> detalles;

    // (Tu clase anidada DetalleVentaDTO se queda igual, está perfecta)
    @Data
    public static class DetalleVentaDTO {

        @NotNull(message = "El idProducto no puede ser nulo")
        private Integer idProducto;

        @NotNull(message = "La cantidad no puede ser nula")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;
    }
}