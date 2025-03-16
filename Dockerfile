# Build Stage
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew build --no-daemon -x test

# Run Stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
