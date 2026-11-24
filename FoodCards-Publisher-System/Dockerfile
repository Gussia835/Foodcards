FROM gradle:8.14-jdk21 AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/config /app/logs /app/incoming /app/target

COPY --from=builder /app/build/libs/mealcard-publisher.jar app.jar

EXPOSE 666 6666

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

ENTRYPOINT ["java", \
            "-Dfile.encoding=UTF-8", \
            "-Dspring.config.additional-location=optional:file:/app/config/application.yml", \
            "-jar", "app.jar"]