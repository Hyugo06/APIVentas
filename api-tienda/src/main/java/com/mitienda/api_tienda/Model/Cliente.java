package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clientes")
@JsonIgnoreProperties({"hibernateLazyInitializer"}) // <-- ¡AÑADE ESTO!
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCliente;

    @NotEmpty(message = "El campo 'nombres' no puede estar vacío")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombres;

    @NotEmpty(message = "El campo 'apellidos' no puede estar vacío")
    @Column(nullable = false, length = 150)
    private String apellidos;

    @NotNull(message = "El DNI no puede ser nulo")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
    @Column(length = 8, nullable = false, unique = true)
    private String dni;

    @NotNull(message = "El celular no puede ser nulo")
    @Pattern(regexp = "^[0-9]{9}$", message = "El celular debe tener exactamente 9 dígitos numéricos")
    @Column(length = 9, nullable = false)
    private String celular;

    @Email(message = "El formato del email es inválido")
    @Column(unique = true, length = 255)
    private String email;

    // Mapeamos la fecha de registro.
    // 'updatable = false' e 'insertable = false' le dicen a JPA
    // que esta columna la gestiona la base de datos (por el 'DEFAULT NOW()')
    @Column(name = "fecha_registro", updatable = false, insertable = false)
    private LocalDateTime fechaRegistro;

    // Nota: Las relaciones (como con 'Ventas') se añadirían aquí
    // @OneToMany(mappedBy = "cliente")
    // private List<Venta> ventas;
}