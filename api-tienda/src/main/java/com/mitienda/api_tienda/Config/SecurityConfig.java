package com.mitienda.api_tienda.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// --- IMPORTACIONES NUEVAS ---
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

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
                // Le decimos a Security que use nuestra configuración de abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // (Tus reglas de siempre)
                        .requestMatchers("/media/**").permitAll()
                        .requestMatchers("/api/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/marcas").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ventas").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/clientes").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers("/api/usuarios/me").authenticated()
                        .requestMatchers("/api/admin/categorias/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/marcas/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/productos/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/ventas/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/clientes/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/cupones/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/usuarios/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/dashboard/**").hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated() // O permitAll() si estás depurando
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}