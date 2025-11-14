package com.mitienda.api_tienda.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Usa @Data de Lombok o @Getter/@Setter
public class ClienteRequestDTO {

    @NotEmpty(message = "El DNI no puede estar vacío")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos")
    private String dni;

    @NotEmpty(message = "El nombre del cliente no puede estar vacío")
    private String nombres;

    @NotEmpty(message = "El apellido del cliente no puede estar vacío")
    private String apellidos;

    @NotEmpty(message = "El celular no puede estar vacío")
    @Pattern(regexp = "^[0-9]{9}$", message = "El celular debe tener 9 dígitos")
    private String celular;

    // El email es opcional, pero si viene, debe ser válido
    @Pattern(regexp = ".+@.+\\..+", message = "Email inválido")
    private String email;
}