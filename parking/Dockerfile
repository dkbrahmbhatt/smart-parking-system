# Stage 1: Build the application using a Maven image
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /parking
COPY pom.xml .
COPY src src
# Use the Maven command directly, as Maven is guaranteed to be installed here
RUN mvn clean package -DskipTests

# Stage 2: Create the final lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /parking
# Copy the built JAR from the build stage
COPY --from=build /parking/target/*.jar app.jar

# Railway sets the PORT env variable; we use it here
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]