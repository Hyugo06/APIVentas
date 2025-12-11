package com.mitienda.api_tienda.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority; // <-- ¡IMPORTA!
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- ¡IMPORTA!
import org.springframework.security.core.userdetails.UserDetails; // <-- ¡IMPORTA!
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collection; // <-- ¡IMPORTA!
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import java.util.stream.Collectors; // <-- ¡IMPORTA!

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    @Column(length = 150)
    private String nombres;

    @Column(length = 150)
    private String apellidos;

    @Column(length = 9)
    private String celular;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> permisos = new ArrayList<>();

    private Boolean activo = true;

    @Column(name = "fecha_creacion", updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Venta> ventas;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.rol.toUpperCase()));
        if (this.permisos != null) {
            for (String permiso : this.permisos) {
                // Añadimos el permiso tal cual, ej: "VER_PRODUCTOS"
                authorities.add(new SimpleGrantedAuthority(permiso.toUpperCase()));
            }
        }
        return authorities;
    }

    @NotEmpty(message = "El nombreUsuario no puede estar vacío")

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