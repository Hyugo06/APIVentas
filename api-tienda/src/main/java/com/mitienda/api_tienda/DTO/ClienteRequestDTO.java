package com.mitienda.api_tienda.DTO;

import lombok.Getter;
import lombok.Setter;

// Este DTO representa los datos del formulario de checkout
@Getter
@Setter
public class ClienteRequestDTO {
    private String nombres;
    private String apellidos;
    private String dni;
    private String celular;
    private String email;
}