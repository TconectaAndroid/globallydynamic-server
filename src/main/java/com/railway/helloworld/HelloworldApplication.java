package com.railway.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class HelloworldApplication {

    // Cache para almacenar APKs descargados
    private final Map<String, byte[]> apkCache = new ConcurrentHashMap<>();
    
    // URLs de tus APKs en Netlify
    private final Map<String, String> moduleUrls = Map.of(
        "dynamicfeature", "https://tconectahost.netlify.app/modules/dynamicfeature-debug.apk",
        "dynamicfeature-debug", "https://tconectahost.netlify.app/modules/dynamicfeature-debug.apk"
    );

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
                    "modules_metadata": "/api/v1/modules/metadata",
                    "download": "/download",
                    "upload": "/upload"
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
                    "name": "dynamicfeature",
                    "version": 1,
                    "url": "https://globallydynamic-server-production.up.railway.app/download",
                    "size": 5952532,
                    "description": "Módulo dinámico con funcionalidades adicionales",
                    "minAppVersion": 1
                },
                {
                    "name": "dynamicfeature-debug",
                    "version": 1,
                    "url": "https://globallydynamic-server-production.up.railway.app/download",
                    "size": 5952532,
                    "description": "Módulo dinámico debug",
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

    // Endpoint que GloballyDynamic usa para descargar módulos - CORREGIDO
    @PostMapping("/download")
    public ResponseEntity<Resource> downloadModule(
            @RequestParam("variant") String variant,
            @RequestParam("version") String version,
            @RequestParam("application-id") String applicationId,
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "features", required = false) String features,
            @RequestBody(required = false) String deviceSpec) {
        
        System.out.println("=== GloballyDynamic Download Request ===");
        System.out.println("Variant: " + variant);
        System.out.println("Version: " + version);
        System.out.println("App ID: " + applicationId);
        System.out.println("Signature: " + signature);
        System.out.println("Features: " + features);
        System.out.println("Device Spec: " + deviceSpec);
        
        try {
            if (!StringUtils.hasText(features)) {
                System.err.println("Features parameter is required");
                return ResponseEntity.badRequest().build();
            }

            // Obtener la URL del módulo
            String apkUrl = moduleUrls.get(features);
            if (apkUrl == null) {
                System.err.println("Feature no encontrado: " + features);
                return ResponseEntity.notFound().build();
            }

            // Descargar el APK desde Netlify y servirlo
            byte[] apkData = downloadApkFromUrl(apkUrl, features);
            if (apkData == null) {
                System.err.println("Error al descargar APK desde: " + apkUrl);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            System.out.println("✓ APK descargado exitosamente, tamaño: " + apkData.length + " bytes");

            // Servir el APK directamente
            ByteArrayResource resource = new ByteArrayResource(apkData);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.android.package-archive")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + features + ".apk\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(apkData.length))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(resource);
                
        } catch (Exception e) {
            System.err.println("Error en download: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Método auxiliar para descargar APK desde Netlify
    private byte[] downloadApkFromUrl(String apkUrl, String featureName) {
        try {
            // Verificar si ya está en cache
            if (apkCache.containsKey(featureName)) {
                System.out.println("APK encontrado en cache para: " + featureName);
                return apkCache.get(featureName);
            }

            System.out.println("Descargando APK desde: " + apkUrl);
            
            URL url = new URL(apkUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000); // 30 segundos
            connection.setReadTimeout(60000);    // 60 segundos
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Error HTTP al descargar APK: " + responseCode);
                return null;
            }

            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                byte[] apkData = outputStream.toByteArray();
                
                // Guardar en cache
                apkCache.put(featureName, apkData);
                
                return apkData;
            }
            
        } catch (Exception e) {
            System.err.println("Error al descargar APK: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Endpoint para subir bundles (implementación básica)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadBundle(
            @RequestParam("variant") String variant,
            @RequestParam("version") String version,
            @RequestParam("application-id") String applicationId,
            @RequestBody byte[] bundleData) {
        
        System.out.println("=== Upload Bundle Request ===");
        System.out.println("Variant: " + variant);
        System.out.println("Version: " + version);
        System.out.println("App ID: " + applicationId);
        System.out.println("Bundle size: " + (bundleData != null ? bundleData.length : 0) + " bytes");
        
        try {
            if (bundleData == null || bundleData.length == 0) {
                return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("{\"error\": \"No bundle data provided\"}");
            }

            // Aquí podrías guardar el bundle en un storage persistente
            // Por ahora solo lo confirmamos
            
            String response = """
                {
                    "status": "success",
                    "message": "Bundle uploaded successfully",
                    "variant": "%s",
                    "version": "%s",
                    "size": %d,
                    "timestamp": "%s"
                }
                """.formatted(
                    variant, 
                    version, 
                    bundleData.length,
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(response);
                
        } catch (Exception e) {
            System.err.println("Error en upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body("{\"error\": \"Internal server error\"}");
        }
    }

    // Endpoint de health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String healthResponse = String.format("""
            {
                "status": "UP",
                "service": "GloballyDynamic Server",
                "version": "1.0.0",
                "timestamp": "%s",
                "cache_size": %d
            }
            """, 
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            apkCache.size());
        
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
                        "name": "dynamicfeature",
                        "status": "available",
                        "download_url": "https://globallydynamic-server-production.up.railway.app/download"
                    },
                    {
                        "name": "dynamicfeature-debug",
                        "status": "available",
                        "download_url": "https://globallydynamic-server-production.up.railway.app/download"
                    }
                ],
                "total_count": 2
            }
            """;
        
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

    // Endpoint para limpiar cache (útil para desarrollo)
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        int size = apkCache.size();
        apkCache.clear();
        
        String response = String.format("""
            {
                "status": "success",
                "message": "Cache cleared",
                "cleared_entries": %d,
                "timestamp": "%s"
            }
            """, size, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(response);
    }
}
