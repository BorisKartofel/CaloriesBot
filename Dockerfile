FROM maven:3.9.6-eclipse-temurin-21
ADD target/Calories_Bot-0.0.1-SNAPSHOT.jar backend.jar
ENTRYPOINT ["java", "-jar", "/backend.jar"]