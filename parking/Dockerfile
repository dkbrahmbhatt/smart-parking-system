# --- Stage 1: Build the Application ---
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /parking

# Copy the pom.xml FIRST to allow Docker to cache dependency downloads
COPY /parking/pom.xml .

# Use the globally available 'mvn' command for compilation
RUN mvn clean package -DskipTests

# --- Stage 2: Create the Final Runtime Image ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /parking

# COPY the built JAR file from the build stage 
# REPLACE 'your-app-name-0.0.1-SNAPSHOT.jar' with the exact filename
COPY --from=build /parking/target/parking-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]