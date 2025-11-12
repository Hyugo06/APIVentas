package com.mitienda.api_tienda.Service;

import com.mitienda.api_tienda.Model.Usuario;
import com.mitienda.api_tienda.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // ¡Importante!
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Este método es llamado por Spring Security
     * cuando un usuario intenta iniciar sesión.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // Buscamos el usuario en nuestra BD por su nombreUsuario
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con nombre: " + username)
                );

        // Como Usuario implementa UserDetails, podemos devolverlo directamente
        return usuario;
    }
}