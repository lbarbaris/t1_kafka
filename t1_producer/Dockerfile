FROM gradle:8.4-jdk17 AS build

WORKDIR /t1_producer
COPY . .

RUN ./gradlew build --no-daemon -x test

FROM openjdk:17-jdk-slim

WORKDIR /t1_producer

COPY --from=build /t1_producer/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
