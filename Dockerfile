FROM openjdk:11-jre-slim

# Información del contenedor
LABEL maintainer="TConecta <tu-email@dominio.com>"
LABEL description="GloballyDynamic Server para módulos dinámicos de TConecta"
LABEL version="1.0.0"

# Variables de entorno
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8080
ENV NETLIFY_BASE_URL=https://tconectahost.netlify.app

# Crear usuario no-root para seguridad
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Crear directorio de trabajo
WORKDIR /app

# Copiar el JAR compilado
COPY target/globallydynamic-server-1.0.0.jar app.jar

# Cambiar propietario de los archivos
RUN chown -R appuser:appuser /app

# Cambiar a usuario no-root
USER appuser

# Exponer puerto
EXPOSE $SERVER_PORT

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:$SERVER_PORT/health || exit 1

# Comando de inicio
ENTRYPOINT ["java", "-Dserver.port=${SERVER_PORT}", "-jar", "app.jar"]