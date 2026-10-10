FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app

COPY settings.gradle ./
COPY build.gradle ./

COPY App-adapter/build.gradle ./App-adapter/
COPY App-adapter/src ./App-adapter/src

RUN gradle :app-adapter:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/App-adapter/build/libs/*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
