package com.mitienda.api_tienda.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ClienteRequestDTO {

    // Eliminamos el @Pattern estricto para que permita recibir 'null' sin bloquearse
    private String dni;

    // Servirá tanto para Nombre del cliente como para la Razón Social de la empresa
    @NotEmpty(message = "El nombre o razón social no puede estar vacío")
    private String nombres;

    // Ya es opcional
    private String apellidos;

    // 👇 ELIMINAMOS @NotEmpty y @Pattern. Ahora pasará limpio hacia tu Service
    private String celular;

    @Email(message = "El formato del email es inválido")
    private String email;
}