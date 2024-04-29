#FROM maven:3.9.6-eclipse-temurin-21 as builder
#WORKDIR /app
#COPY . /app/.
#RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=truedocker
#
#FROM maven:3.9.6-eclipse-temurin-21
#WORKDIR /app
#COPY --from=builder /app/target/*.jar /app/*.jar
#EXPOSE 8080
#CMD ["java", "-jar", "*.jar"]
#TODO Команда для билда
FROM maven:3.9.6-eclipse-temurin-21
ADD target/Calories_Bot-0.0.1-SNAPSHOT.jar backend.jar
ENTRYPOINT ["java", "-jar", "/backend.jar"]