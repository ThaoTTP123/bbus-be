# Use an official Maven image to build the application
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use a minimal JDK 17 runtime image to run the application
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]



