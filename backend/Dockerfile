FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system workmate && useradd --system --gid workmate workmate

COPY target/*.jar /app/workmate-ai-backend.jar

EXPOSE 8080
USER workmate

ENTRYPOINT ["java", "-jar", "/app/workmate-ai-backend.jar"]
