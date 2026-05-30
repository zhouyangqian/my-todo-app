FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY services/permission-service/target/permission-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
