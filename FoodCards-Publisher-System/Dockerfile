FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY settings.gradle ./
COPY build.gradle ./

COPY FoodCards-Publisher-System/build.gradle ./FoodCards-Publisher-System/
COPY FoodCards-Publisher-System/src ./FoodCards-Publisher-System/src

RUN gradle :publisher:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/FoodCards-Publisher-System/build/libs/*.jar app.jar

EXPOSE 666 6666
CMD ["java", "-jar", "app.jar"]
