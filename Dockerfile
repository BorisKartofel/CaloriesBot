# Dependency installation
RUN mvn clean install

# Run Spring app
CMD mvn spring-boot:run