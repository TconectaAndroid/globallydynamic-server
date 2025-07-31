package com.railway.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class HelloworldApplication {

    public static void main(String[] args) {
        System.out.println("=== Iniciando GloballyDynamic Server para TConecta ===");
        SpringApplication.run(HelloworldApplication.class, args);
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String response = String.format("""
            {
                "service": "GloballyDynamic Server",
                "status": "running",
                "version": "1.0.0",
                "timestamp": "%s",
                "endpoints": {
                    "health": "/health",
                    "modules_metadata": "/api/v1/modules/metadata"
                }
            }
            """, timestamp);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(response);
    }

    // Endpoint principal para GloballyDynamic - Metadata de módulos
    @GetMapping("/api/v1/modules/metadata")
    public ResponseEntity<String> getModulesMetadata() {
        System.out.println("=== Solicitando metadata de módulos ===");
        
        String jsonResponse = """
            {
                "version": 1,
                "modules": [
                    {
                        "name": "extension_pagos_servicios",
                        "version": 2,
                        "url": "https://tconectahost.netlify.app/modules/extension_pagos_servicios-debug.apk",
                        "size": 425984,
                        "description": "Módulo dinámico con funcionalidades adicionales",
                        "minAppVersion": 1
                    }
                ]
            }
            """;
        
        System.out.println("✓ Metadata devuelta exitosamente");
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
            .body(jsonResponse);
    }

    // Endpoint de health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String healthResponse = String.format("""
            {
                "status": "UP",
                "service": "GloballyDynamic Server",
                "version": "1.0.0",
                "timestamp": "%s"
            }
            """, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(healthResponse);
    }

    @GetMapping("/api/v1/modules/list")
    public ResponseEntity<String> listAvailableModules() {
        System.out.println("=== Listando módulos disponibles ===");
        
        String response = """
            {
                "available_modules": [
                    {
                        "name": "extension_pagos_servicios",
                        "status": "available",
                        "download_url": "https://tconectahost.netlify.app/modules/extension_pagos_servicios-debug.apk"
                    }
                ],
                "total_count": 1
            }
            """;
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(response);
    }
}
