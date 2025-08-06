# Etapa 1: build del proyecto con Gradle
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
# RUN gradle clean bootJar --no-daemon
# RUN ./gradlew clean bootJar --no-daemon
RUN gradle --no-daemon --stacktrace --info clean bootJar

# Etapa 2: imagen liviana para ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copiar el jar generado (detectado automáticamente con wildcard)
COPY --from=builder /app/build/libs/*.jar app.jar
# Expone el puerto interno del contenedor
EXPOSE 8095
# Entrypoint configurable con el perfil activo (ej: rabbit o kafka)
ENTRYPOINT ["sh", "-c", "java -Dserver.port=8095 -Dspring.profiles.active=${SPRING_PROFILE} -Dh2.console.web-allow-others=true -jar app.jar"]

# docker build -t ecommerce-api .
# docker run -e SPRING_PROFILE=rabbit -p 8093:8093 --name ecommerce ecommerce-api
