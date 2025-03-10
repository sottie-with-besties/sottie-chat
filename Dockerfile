FROM openjdk:17-jdk-slim

COPY target/sottie-chat.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]