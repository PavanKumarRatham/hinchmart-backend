FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# The GitHub Actions build produces this executable Spring Boot JAR before the
# image is built. No source files, .env file, or credentials are copied in.
COPY target/hinchmart-backend-*.jar app.jar

ENV PORT=9000

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
