FROM openjdk:11.0.10-jdk-slim-buster

COPY build/libs/reactive-starwars*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]