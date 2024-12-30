FROM gradle:7.5-jdk17-alpine AS build
WORKDIR /app

COPY gradle /app/gradle
COPY gradlew /app/gradlew
COPY build.gradle /app/build.gradle
COPY settings.gradle /app/settings.gradle

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY . .
RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]