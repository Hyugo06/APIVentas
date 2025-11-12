package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority; // <-- ¡IMPORTA!
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- ¡IMPORTA!
import org.springframework.security.core.userdetails.UserDetails; // <-- ¡IMPORTA!

import java.time.LocalDateTime;
import java.util.Collection; // <-- ¡IMPORTA!
import java.util.List;
import java.util.stream.Collectors; // <-- ¡IMPORTA!

@Data
@Entity
@Table(name = "usuarios")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Usuario implements UserDetails { // <-- ¡IMPLEMENTA UserDetails!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @NotEmpty(message = "El nombreUsuario no puede estar vacío")
    @Column(nullable = false, unique = true, length = 100)
    private String nombreUsuario;

    @NotEmpty(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String hashContrasena;

    @Column(nullable = false, length = 50)
    private String rol;

    private Boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Venta> ventas;

    // --- MÉTODOS DE UserDetails REQUERIDOS POR SPRING SECURITY ---

    /**
     * Convierte nuestro campo 'rol' (ej. "ADMIN") en un permiso
     * que Spring Security entiende (ej. "ROLE_ADMIN").
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Debemos anteponer "ROLE_" a nuestro rol para que Spring lo reconozca
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + this.rol)
        );
        return authorities;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.hashContrasena; // Le dice a Spring cuál es el campo de la contraseña
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.nombreUsuario; // Le dice a Spring cuál es el campo del nombre de usuario
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true; // Podemos dejarlo como true
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true; // Podemos dejarlo como true
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true; // Podemos dejarlo como true
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.activo; // Usamos nuestro campo 'activo'
    }
}