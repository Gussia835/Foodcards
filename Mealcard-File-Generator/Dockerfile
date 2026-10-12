FROM gradle:8.14-jdk21 AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./

COPY Mealcard-File-Generator/build.gradle ./Mealcard-File-Generator/
COPY Mealcard-File-Generator/src ./Mealcard-File-Generator/src

RUN chmod +x gradlew
RUN ./gradlew :generator:shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/config /app/logs /app/out/cards

COPY --from=builder /app/Mealcard-File-Generator/build/libs/mealcard-generator.jar app.jar

EXPOSE 90 6666

ENTRYPOINT ["java", \
            "-Dfile.encoding=UTF-8", \
            "-Dapp.config=/app/config/app.properties", \
            "-Dlogback.configurationFile=/app/config/logback.xml", \
            "-jar", "app.jar"]