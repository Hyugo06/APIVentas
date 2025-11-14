package com.mitienda.api_tienda.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer; // <-- ¡ASEGÚRATE DE IMPORTAR ESTO!
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // --- ¡¡AÑADE ESTA LÍNEA AQUÍ!! ---
                // Le dice a Spring Security que busque y aplique
                // la configuración CORS (que está en tu WebConfig.java)
                .cors(Customizer.withDefaults())
                // ---------------------------------

                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize

                        // (Tus reglas de seguridad que ya están bien)
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/marcas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ventas").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers("/api/usuarios/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMIN")
                        .anyRequest().authenticated() // (Cambiado de permitAll a authenticated)
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}