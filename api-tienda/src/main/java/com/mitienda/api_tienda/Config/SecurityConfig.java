package com.mitienda.api_tienda.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // ¡Importa HttpMethod!
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
// --- ¡Ya NO necesitamos esto! ---
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // --- 1. ENCRIPTADOR (Se queda igual) ---
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- 2. USUARIOS DE PRUEBA (¡ELIMINADO!) ---
    /**
     * Este método @Bean de 'userDetailsService' (InMemoryUserDetailsManager)
     * se elimina por completo.
     * * Spring Boot detectará automáticamente nuestro
     * 'CustomUserDetailsService' porque implementa UserDetailsService
     * y está anotado con @Service.
     */
    // @Bean  <-- ¡BORRA TODO ESTE MÉTODO!
    // public UserDetailsService userDetailsService() { ... }


    // --- 3. REGLAS DE ACCESO (Se queda casi igual) ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // --- ¡¡CAMBIO AQUÍ!! ---
                // Vamos a borrar todas las reglas específicas
                // y a permitir todo temporalmente.
                .authorizeHttpRequests(authorize -> authorize

                        // BORRA TODAS LAS LÍNEAS .requestMatchers(...)

                        // Y AÑADE SOLO ESTA:
                        .anyRequest().permitAll() // <-- Permite CUALQUIER petición
                )
                // --- FIN DEL CAMBIO ---

                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}