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
                        "name": "extension_pagos_servicios_s",
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
                        "name": "extension_pagos_servicios_s",
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

    // Endpoint que GloballyDynamic usa para descargar módulos
@PostMapping("/download")
public ResponseEntity<byte[]> downloadModule(
        @RequestParam("variant") String variant,
        @RequestParam("version") String version,
        @RequestParam("application-id") String applicationId,
        @RequestParam(value = "features", required = false) String features,
        @RequestBody(required = false) String deviceSpec) {
    
    System.out.println("=== GloballyDynamic Download Request ===");
    System.out.println("Variant: " + variant);
    System.out.println("Version: " + version);
    System.out.println("App ID: " + applicationId);
    System.out.println("Features: " + features);
    
    try {
        // Aquí simularemos devolver el APK desde Netlify
        // En producción, deberías descargar el archivo real
        String apkUrl = "https://tconectahost.netlify.app/modules/extension_pagos_servicios-debug.apk";
        
        // Por ahora, devolvemos una respuesta simulada
        // TODO: Implementar descarga real del APK
        byte[] apkData = "APK_DATA_PLACEHOLDER".getBytes();
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/zip")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=splits.zip")
            .body(apkData);
            
    } catch (Exception e) {
        System.err.println("Error en download: " + e.getMessage());
        return ResponseEntity.status(500).build();
    }
}

// Endpoint para subir bundles (desarrollo)
@PostMapping("/upload")
public ResponseEntity<String> uploadBundle(@RequestBody(required = false) byte[] bundleData) {
    System.out.println("=== Upload Bundle Request ===");
    
    // Simular upload exitoso
    String response = """
        {
            "status": "success",
            "message": "Bundle uploaded successfully",
            "timestamp": "%s"
        }
        """.formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .body(response);
}

// Endpoint para verificar si el servidor está corriendo
@GetMapping("/liveness_check")
public ResponseEntity<String> livenessCheck() {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, "text/plain")
        .body("OK");
}
    
}
