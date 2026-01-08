# Use official Java 17 base image
FROM openjdk:17-jdk

# Set working directory
WORKDIR /app

# Copy your jar file into container and rename it
COPY target/realWordJob-0.0.1-SNAPSHOT.jar /app/dockerdemo.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "/app/dockerdemo.jar"]
