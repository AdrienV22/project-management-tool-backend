package com.example.project_management_tool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // Annoncer cette classe comme une classe de configuration Spring
public class WebConfig implements WebMvcConfigurer {

    // Cette méthode permet de configurer CORS pour toute l'application
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // La règle suivante applique CORS à tous les endpoints de l'application
        registry.addMapping("/**") // Appliquer à tous les endpoints
                .allowedOrigins("http://localhost:4200") // Autoriser les requêtes venant de localhost:4200 (ton frontend Angular)
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Autoriser les méthodes HTTP que tu utilises
                .allowedHeaders("*") // Autoriser tous les en-têtes (si tu en as besoin)
                .allowCredentials(true); // Autoriser l'envoi de cookies si nécessaire
    }
}
