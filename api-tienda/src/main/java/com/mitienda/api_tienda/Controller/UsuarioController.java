package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.Model.Usuario;
import com.mitienda.api_tienda.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios") // URL base
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Endpoints Específicos de VENDEDOR ---

    /**
     * GET /api/usuarios/vendedores
     * Obtiene una lista de SOLO los vendedores.
     */
    @GetMapping("/vendedores")
    public List<Usuario> obtenerVendedores() {
        return usuarioService.obtenerVendedores();
    }

    /**
     * POST /api/usuarios/vendedor
     * Crea un nuevo VENDEDOR.
     */
    @PostMapping("/vendedor")
    public ResponseEntity<?> crearVendedor(@Valid @RequestBody Usuario usuario) {
        try {
            // 1. Forzamos el rol (Esto es un endpoint específico para el vendedor)
            usuario.setRol("VENDEDOR");

            // 2. Llamamos al método genérico que ya creamos en el servicio
            Usuario nuevoVendedor = usuarioService.crearUsuario(usuario);

            return new ResponseEntity<>(nuevoVendedor, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario nuevoUsuario) {

        // Lógica para asegurar que el rol existe (si no viene del formulario)
        String rol = nuevoUsuario.getRol();
        if (rol == null || rol.trim().isEmpty()) {
            // Opción segura por defecto
            nuevoUsuario.setRol("VENDEDOR");
        } else {
            // Asegurar que el rol esté en mayúsculas (por consistencia con Spring Security)
            nuevoUsuario.setRol(rol.toUpperCase());
        }

        // ✅ ¡CAMBIO CLAVE! Llama al nuevo método genérico
        Usuario usuarioCreado = usuarioService.crearUsuario(nuevoUsuario);

        return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED);
    }

    // --- Endpoints Genéricos de USUARIO ---

    /**
     * GET /api/usuarios
     * Obtiene TODOS los usuarios (admins y vendedores).
     */
    @GetMapping
    public List<Usuario> obtenerTodos() {
        return usuarioService.obtenerTodosLosUsuarios();
    }

    /**
     * GET /api/usuarios/{id}
     * Obtiene cualquier usuario por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Integer id) {
        // La entidad Usuario ya tiene @JsonProperty(access = WRITE_ONLY) en el hash,
        // lo cual lo oculta de la respuesta, manteniendo la seguridad.
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> getUsuarioActual(Principal principal) {
        // 'Principal' es un objeto de Spring Security que contiene el username del token
        Usuario usuario = (Usuario) usuarioService.loadUserByUsername(principal.getName());
        return ResponseEntity.ok(usuario);
    }

    /**
     * PUT /api/usuarios/{id}
     * Actualiza un usuario por ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario detallesUsuario) {

        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuarioExistente = usuarioOpt.get();

        // 1. Actualizar campos simples
        usuarioExistente.setNombreUsuario(detallesUsuario.getNombreUsuario());

        usuarioExistente.setNombres(detallesUsuario.getNombres());
        usuarioExistente.setApellidos(detallesUsuario.getApellidos());
        usuarioExistente.setCelular(detallesUsuario.getCelular());

        usuarioExistente.setRol(detallesUsuario.getRol().toUpperCase());
        usuarioExistente.setActivo(detallesUsuario.getActivo());

        // 2. Manejo SEGURO de la contraseña
        // Si el cliente envia una nueva contraseña (hashContrasena != null)
        if (detallesUsuario.getHashContrasena() != null && !detallesUsuario.getHashContrasena().isEmpty()) {
            // ESTA LÍNEA ES CLAVE: Encriptar la nueva contraseña.
            String nuevoHash = passwordEncoder.encode(detallesUsuario.getHashContrasena());
            usuarioExistente.setHashContrasena(nuevoHash);
        }

        // 3. Guardar y devolver
        return ResponseEntity.ok(usuarioService.guardarUsuario(usuarioExistente));
    }

    /**
     * DELETE /api/usuarios/{id}
     * Desactiva (Soft Delete) un usuario por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Integer id) { // <-- ¡CORREGIDO!
        if (usuarioService.desactivarUsuario(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}