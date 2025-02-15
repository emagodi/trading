# Use an official OpenJDK image as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the specific JAR file from your local machine to the container
COPY target/trading-0.0.1-SNAPSHOT.jar app.jar

# Expose the ports for both applications
EXPOSE 8080 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]