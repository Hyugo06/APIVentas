# Etapa 1: Construcción (Usamos una imagen con Maven para compilar)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Damos permisos al mvnw por si acaso, aunque usaremos el maven de la imagen
RUN chmod +x mvnw
# Compilamos el proyecto saltando los tests para ir más rápido
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Usamos una imagen ligera solo con Java para correr la app)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copiamos el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto 8080
EXPOSE 8080
# Comando para arrancar la app
ENTRYPOINT ["java", "-jar", "app.jar"]