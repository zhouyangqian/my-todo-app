FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY services/dict-service/target/dict-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
