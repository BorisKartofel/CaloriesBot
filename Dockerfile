FROM maven:3.9.6-eclipse-temurin-21
COPY target/Calories_Bot-0.0.2.jar application.jar
ENTRYPOINT ["java", "-jar", "/application.jar"]