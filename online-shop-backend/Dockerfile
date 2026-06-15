FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN addgroup --system shop && adduser --system --ingroup shop shop
COPY --from=build /workspace/target/online-shop-backend-1.0.0-SNAPSHOT.jar app.jar
USER shop
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
