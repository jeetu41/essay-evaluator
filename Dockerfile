# Build stage
FROM maven:3.8.6-openjdk-18-slim AS build
WORKDIR /app

# cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:18-jre-alpine
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080  # optional, safe for local dev
ENTRYPOINT ["java", "-jar", "app.jar"]

