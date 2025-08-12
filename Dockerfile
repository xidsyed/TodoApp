# Use a minimal JVM base image
FROM eclipse-temurin:21-jdk-jammy

# Set work directory
WORKDIR /app

# Copy build output
COPY build/libs/todoapp-*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
