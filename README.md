# GloballyDynamic Server para TConecta

Servidor personalizado para servir módulos dinámicos de Android usando GloballyDynamic, integrado con Netlify como CDN.

## 🚀 Características

- **Servidor Spring Boot** optimizado para Railway
- **Integración con Netlify** para servir APKs
- **API REST** compatible con GloballyDynamic
- **Health checks** y monitoreo
- **Logs detallados** para debugging
- **Docker support** incluido

## 📁 Estructura del Proyecto

```
globallydynamic-server/
├── src/main/java/com/tconecta/server/
│   └── GloballyDynamicServerApplication.java
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── railway.json
├── Dockerfile
└── README.md
```

## 🛠️ Setup Local

### Prerrequisitos
- Java 11 o superior
- Maven 3.6+
- Acceso a internet

### Pasos de instalación

1. **Clonar el repositorio**
```bash
git clone <tu-repo>
cd globallydynamic-server
```

2. **Compilar el proyecto**
```bash
mvn clean package
```

3. **Ejecutar localmente**
```bash
java -jar target/globallydynamic-server-1.0.0.jar
```

4. **Verificar funcionamiento**
```bash
curl http://localhost:8080/health
```

## 🚂 Deploy en Railway

### Opción 1: Deploy desde GitHub (Recomendado)

1. **Sube el código a GitHub**
2. **Ve a Railway.app** y conecta tu cuenta de GitHub
3. **Crear nuevo proyecto**: "Deploy from GitHub repo"
4. **Selecciona tu repositorio**
5. **Railway detectará automáticamente** la configuración desde `railway.json`

### Opción 2: Deploy con Railway CLI

```bash
# Instalar Railway CLI
npm install -g @railway/cli

# Login
railway login

# Deploy
railway up
```

## 🔧 Configuración

### Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `PORT` | Puerto del servidor | `8080` |
| `NETLIFY_BASE_URL` | URL base de Netlify | `https://tconectahost.netlify.app` |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring | `production` |

### Configuración en Railway

En el dashboard de Railway, ve a **Variables** y agrega:

```
NETLIFY_BASE_URL=https://tconectahost.netlify.app
SPRING_PROFILES_ACTIVE=production
```

## 📡 API Endpoints

### Información del servidor
```
GET /
```

### Health Check
```
GET /health
GET /actuator/health
```

### Metadata de módulos
```
GET /api/v1/modules/metadata
```

### Descargar módulo
```
GET /api/v1/modules/download?name=extension_pagos_servicios
```

### Listar módulos disponibles
```
GET /api/v1/modules/list
```

## 🔗 Integración con Android

### Configuración en build.gradle.kts

```kotlin
globallyDynamicServers {
    serverUrl = "https://tu-app-production.up.railway.app"
    applyToBuildVariants("debug", "release")
}

dependencies {
    implementation("com.jeppeman.globallydynamic.android:selfhosted:1.0.0")
}
```

### Uso en CustomModuleManager

```java
public void getAvailableModules(ModulesCallback callback) {
    String url = "https://tu-app-production.up.railway.app/api/v1/modules/metadata";
    // ... resto del código
}
```

## 🐛 Debugging

### Logs en Railway

1. Ve a tu proyecto en Railway
2. Click en **"Deployments"**
3. Selecciona el deployment activo
4. Ver **"Logs"** en tiempo real

### Logs locales

```bash
# Ver logs en tiempo real
tail -f logs/application.log

# Verificar conectividad con Netlify
curl https://tu-app-production.up.railway.app/health
```

### Endpoints de debug

```bash
# Verificar metadata
curl https://tu-app-production.up.railway.app/api/v1/modules/metadata

# Verificar descarga de módulo
curl -I https://tu-app-production.up.railway.app/api/v1/modules/download?name=extension_pagos_servicios
```

## 🔒 Seguridad

- **Usuario no-root** en Docker
- **CORS configurado** para requests cross-origin
- **Headers de seguridad** básicos incluidos
- **Timeout configurations** para evitar DoS

## 📊 Monitoreo

### Health Checks incluidos

- **Servidor Spring Boot**: Verifica que el servidor esté corriendo
- **Conexión con Netlify**: Verifica conectividad con tu CDN
- **Memoria y sistema**: Via Spring Actuator

### Métricas disponibles

```bash
curl https://tu-app-production.up.railway.app/actuator/health
```

## 🚨 Troubleshooting

### Problemas comunes

1. **Error 404 al descargar módulos**
   - Verificar que el archivo existe en Netlify
   - Verificar URL en `NETLIFY_BASE_URL`

2. **Timeout en conexiones**
   - Verificar conectividad de red
   - Aumentar timeout en `application.properties`

3. **Error al compilar**
   - Verificar Java 11+ está instalado
   - Ejecutar `mvn clean install`

### Contacto y Soporte

Para problemas específicos, revisar:
1. **Logs de Railway** (deployment logs)
2. **Health endpoint**: `/health`
3. **Netlify status**: Verificar que tu CDN esté funcionando

## 📄 Licencia

Este proyecto es para uso interno de TConecta.

---

**✨ ¡Listo para usar con tu app Android y GloballyDynamic!**