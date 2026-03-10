package com.mitienda.api_tienda.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Permitimos todas las rutas que empiecen con /api/
                registry.addMapping("/api/**")
                        // Permitimos que tu Frontend en la IP 147.93.176.158 se conecte
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // Importante para que el encabezado 'Authorization' pase sin problemas
                        .allowCredentials(false);
            }
        };
    }
}
