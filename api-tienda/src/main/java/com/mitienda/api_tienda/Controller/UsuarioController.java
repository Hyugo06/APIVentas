package com.mitienda.api_tienda.Controller;

import com.mitienda.api_tienda.Model.Usuario;
import com.mitienda.api_tienda.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios") // URL base
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

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
    public ResponseEntity<?> crearVendedor(@Valid @RequestBody Usuario usuario) { // <-- ¡AQUÍ!
        try {
            Usuario nuevoVendedor = usuarioService.crearVendedor(usuario);
            return new ResponseEntity<>(nuevoVendedor, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/usuarios/{id}
     * Actualiza un usuario por ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return usuarioService.actualizarUsuario(id, usuario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/usuarios/{id}
     * Desactiva (Soft Delete) un usuario por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Integer id,
                                                  @Valid @RequestBody Usuario usuario) {
        if (usuarioService.desactivarUsuario(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}