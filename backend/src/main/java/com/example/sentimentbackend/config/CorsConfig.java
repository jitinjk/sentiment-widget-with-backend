package com.example.sentimentbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin:}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var origins = new ArrayList<String>();
        origins.add("http://localhost:5173");
        origins.add("http://localhost:4173");
        if (!allowedOrigin.isBlank()) origins.add(allowedOrigin);

        registry.addMapping("/api/**")
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}
