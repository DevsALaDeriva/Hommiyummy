# Usa una imagen de Maven para compilar el proyecto
FROM maven:3.9.2-eclipse-temurin-17 as build

# Establece el directorio de trabajo
WORKDIR /app

# Copia los archivos del proyecto al contenedor
COPY . .

# Ejecuta Maven para compilar el proyecto y crear el JAR
RUN mvn clean package -DskipTests

# Usa una imagen más ligera de Java para ejecutar la aplicación
FROM eclipse-temurin:17-jdk-jammy

# Establece el directorio de trabajo
WORKDIR /app

# Copia el JAR compilado desde la imagen anterior
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto en el que correrá la aplicación
EXPOSE 8080

# Ejecuta la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

