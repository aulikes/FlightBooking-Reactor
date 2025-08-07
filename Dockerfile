# Etapa final (no compila dentro del Dockerfile)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8095
ENTRYPOINT ["sh", "-c", "java -Dserver.port=8095 -Dspring.profiles.active=${SPRING_PROFILE} -Dh2.console.web-allow-others=true -jar app.jar"]
