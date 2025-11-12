package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.Model.Usuario;
import com.mitienda.api_tienda.Repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    // --- ¡¡INJECTA EL ENCRIPTADOR!! ---
    @Autowired
    private PasswordEncoder passwordEncoder;
    // --- Métodos Específicos para VENDEDORES ---

    /**
     * Obtiene solo los usuarios con rol 'vendedor'
     */
    public List<Usuario> obtenerVendedores() {
        return usuarioRepository.findByRol("vendedor");
    }

    /**
     * Crea un nuevo usuario y le asigna automáticamente el rol 'vendedor'.
     * ¡¡SEGURIDAD!! En una app real, aquí es donde deberías
     * usar un PasswordEncoder para hashear la contraseña antes de guardarla.
     */
    @Transactional
    public Usuario crearUsuario(Usuario nuevoUsuario) {
        // ... (hasheo de contraseña) ...

        // 2. Establecer la fecha de creación y el estado
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());
        nuevoUsuario.setActivo(true);

        // ✅ CORRECCIÓN: Nos aseguramos de que el rol se guarde en MAYÚSCULAS en la BD.
        nuevoUsuario.setRol(nuevoUsuario.getRol().toUpperCase());

        return usuarioRepository.save(nuevoUsuario);
    }


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos el usuario en nuestra BD por su nombreUsuario
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con nombre: " + username)
                );

        // Como Usuario implementa UserDetails, podemos devolverlo directamente
        return usuario;
    }

    // --- Métodos Genéricos de Usuario ---

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Actualiza un usuario. No permite cambiar el rol desde aquí.
     */
    public Optional<Usuario> actualizarUsuario(Integer id, Usuario detallesUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuarioExistente = usuarioOpt.get();
        usuarioExistente.setNombreUsuario(detallesUsuario.getNombreUsuario());

        // (Opcional) Si se provee una nueva contraseña, hashearla y guardarla
        if (detallesUsuario.getHashContrasena() != null && !detallesUsuario.getHashContrasena().isEmpty()) {
            // Aquí iría la lógica de hashing
            usuarioExistente.setHashContrasena(detallesUsuario.getHashContrasena());
        }

        return Optional.of(usuarioRepository.save(usuarioExistente));
    }

    /**
     * Desactiva un usuario (Soft Delete) en lugar de borrarlo.
     * Esto mantiene la integridad de las ventas pasadas.
     */
    public boolean desactivarUsuario(Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(false); // <-- SOFT DELETE
        usuarioRepository.save(usuario);
        return true;
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}