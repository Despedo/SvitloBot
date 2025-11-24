# Use Amazon Corretto 21 as the base image
FROM amazoncorretto:21

# Set the working directory inside the container
WORKDIR /app

# Copy your built JAR file into the container (adjust the filename as needed)
COPY build/libs/*.jar app.jar

# Set the entry point to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]