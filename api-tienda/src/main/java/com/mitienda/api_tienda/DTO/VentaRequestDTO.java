package com.mitienda.api_tienda.DTO;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import com.mitienda.api_tienda.DTO.VentaRequestDTO.DetalleVentaDTO; // Asegura tus imports

@Data
public class VentaRequestDTO {

    @NotNull(message = "Los datos del cliente no pueden ser nulos")
    @Valid
    private ClienteRequestDTO clienteData;

    @NotEmpty(message = "El tipoComprobante no puede estar vacío")
    private String tipoComprobante;

    // --- NUEVO CAMPO AGREGADO ---
    // Recibe el ID del cupón desde el checkout de Angular
    private Integer idCupon;
    // ----------------------------

    @NotNull(message = "La lista de detalles no puede ser nula")
    @NotEmpty(message = "La lista de detalles no puede estar vacía")
    @Valid
    private List<DetalleVentaDTO> detalles;

    @Data
    public static class DetalleVentaDTO {
        @NotNull
        private Integer idProducto;
        private Integer idVariante;
        @NotNull
        @Min(value = 1)
        private Integer cantidad;
    }
}