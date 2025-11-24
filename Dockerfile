
# Build stage
FROM amazoncorretto:21 AS build

# Set the working directory in the build stage
WORKDIR /app

# Copy the Gradle files
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies
RUN ./gradlew --no-daemon dependencies || return 0

# Copy the source code
COPY src ./src

# Build the application
RUN ./gradlew --no-daemon build -x test

# Runtime stage
FROM amazoncorretto:21

# Set the working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]