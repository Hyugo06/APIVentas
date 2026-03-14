package com.mitienda.api_tienda.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClienteRequestDTO {

    // NUEVO: Permite exactamente 8 dígitos (DNI) o 11 dígitos (RUC)
    @Pattern(regexp = "^([0-9]{8}|[0-9]{11})$", message = "El documento debe tener 8 (DNI) o 11 (RUC) dígitos")
    private String dni;

    // Servirá tanto para Nombre del cliente como para la Razón Social de la empresa
    @NotEmpty(message = "El nombre o razón social no puede estar vacío")
    private String nombres;

    // ELIMINAMOS el @NotEmpty. Ahora es opcional porque las empresas no tienen apellidos.
    private String apellidos;

    @NotEmpty(message = "El celular no puede estar vacío")
    @Pattern(regexp = "^[0-9]{9}$", message = "El celular debe tener 9 dígitos")
    private String celular;

    @Email(message = "El formato del email es inválido")
    private String email;
}