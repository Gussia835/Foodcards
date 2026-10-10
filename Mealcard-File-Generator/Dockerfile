FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY settings.gradle ./
COPY build.gradle ./

COPY Mealcard-File-Generator/build.gradle ./Mealcard-File-Generator/
COPY Mealcard-File-Generator/src ./Mealcard-File-Generator/src

RUN gradle :generator:shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/Mealcard-File-Generator/build/libs/*-all.jar app.jar

EXPOSE 90 6666
CMD ["java", "-jar", "app.jar"]
