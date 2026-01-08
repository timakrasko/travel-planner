FROM gradle:8.5-jdk17 AS build
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN gradle clean build --no-daemon -x test

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV SERVER_PORT=4567
EXPOSE 4567

ENTRYPOINT ["java", "-jar", "app.jar"]