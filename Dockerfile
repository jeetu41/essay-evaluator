# ------------------------
# Build Stage
# ------------------------
FROM maven:3.8.6-openjdk-18-slim AS build
WORKDIR /app

# Copy pom.xml and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------
# Run Stage
# ------------------------
FROM eclipse-temurin:18-jre-alpine
WORKDIR /app

# Copy the JAR built in the build stage
COPY --from=build /app/target/app.jar app.jar

# ENTRYPOINT runs the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
