package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.Model.Usuario;
import com.mitienda.api_tienda.Repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
// --- ¡IMPORTACIONES IMPORTANTES QUE FALTABAN! ---
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // <-- ¡Añade esta!
import org.springframework.security.core.userdetails.UsernameNotFoundException; // <-- ¡Añade esta!
// ---------------------------------------------
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
// --- ¡¡CORRECCIÓN 1: Implementar la interfaz!! ---
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Este método es llamado por Spring Security (porque implementamos UserDetailsService)
     * Y también es llamado por nuestro endpoint /me.
     */
    @Override // <-- Ahora esta anotación SÍ es correcta
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos el usuario en nuestra BD por su nombreUsuario
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con nombre: " + username)
                );

        // Como nuestra entidad Usuario implementa UserDetails, la devolvemos.
        return usuario;
    }

    /**
     * Obtiene solo los usuarios con rol 'vendedor'
     */
    public List<Usuario> obtenerVendedores() {
        return usuarioRepository.findByRol("VENDEDOR"); // (Cambiado a mayúsculas por consistencia)
    }
    /**
     * Crea un nuevo usuario. Respeta el ROL que viene del controlador.
     */
    @Transactional
    public Usuario crearUsuario(Usuario nuevoUsuario) {
        // 1. Hashear la contraseña
        String hashedPassword = passwordEncoder.encode(nuevoUsuario.getHashContrasena());
        nuevoUsuario.setHashContrasena(hashedPassword);

        // 2. Establecer la fecha de creación y el estado
        nuevoUsuario.setFechaCreacion(LocalDateTime.now());
        nuevoUsuario.setActivo(true);

        // 3. Forzar el rol a mayúsculas para consistencia
        if (nuevoUsuario.getRol() != null) {
            nuevoUsuario.setRol(nuevoUsuario.getRol().toUpperCase());
        } else {
            nuevoUsuario.setRol("VENDEDOR"); // Rol por defecto si es nulo
        }

        return usuarioRepository.save(nuevoUsuario);
    }

    // --- Métodos Genéricos de Usuario ---

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Actualiza un usuario.
     * (Este método NO actualiza el ROL o ESTADO, eso lo hace el controlador antes de llamar a guardarUsuario)
     */
    public Optional<Usuario> actualizarUsuario(Integer id, Usuario detallesUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuarioExistente = usuarioOpt.get();
        usuarioExistente.setNombreUsuario(detallesUsuario.getNombreUsuario());

        // --- ¡¡CORRECCIÓN 2: Hashear la contraseña!! ---
        if (detallesUsuario.getHashContrasena() != null && !detallesUsuario.getHashContrasena().isEmpty()) {
            String nuevoHash = passwordEncoder.encode(detallesUsuario.getHashContrasena());
            usuarioExistente.setHashContrasena(nuevoHash);
        }
        // ----------------------------------------------

        return Optional.of(usuarioRepository.save(usuarioExistente));
    }

    /**
     * Desactiva un usuario (Soft Delete).
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

    /**
     * Guarda un usuario actualizado (usado por el PUT).
     */
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}