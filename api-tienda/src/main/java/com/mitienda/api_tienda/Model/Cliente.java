package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "clientes")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    // Sirve para Nombres (Persona Natural) o Razón Social (Empresa)
    @NotEmpty(message = "El nombre o razón social no puede estar vacío")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombres;

    // ELIMINAMOS el @NotEmpty porque las empresas (Facturas) NO tienen apellidos.
    // Además, aseguramos el nullable = true para la base de datos.
    @Column(name = "apellidos", nullable = true, length = 150)
    private String apellidos;

    // ACTUALIZADO: Permite 8 dígitos (DNI) o 11 dígitos (RUC).
    // También aumentamos el length de la columna a 11.
    @Column(length = 11, nullable = true, unique = true)
    private String dni;

    @Column(name = "celular", length = 9, nullable = true)
    private String celular;

    @Email(message = "El formato del email es inválido")
    @Column(unique = true, length = 255)
    private String email;

    @Column(name = "fecha_registro", updatable = false, insertable = false)
    private LocalDateTime fechaRegistro;

    @Transient
    private Double deudaActual = 0.0;
}