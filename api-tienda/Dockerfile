# Etapa 1: Construcción (Usamos imagen con Java 21 para compilar)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Damos permisos al mvnw
RUN chmod +x mvnw
# Compilamos
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Usamos imagen ligera de Java 21 para correr la app)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el .jar generado
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto
EXPOSE 8080
# Arrancamos
ENTRYPOINT ["java", "-jar", "app.jar"]