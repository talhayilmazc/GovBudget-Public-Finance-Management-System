FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S govbudget && adduser -S govbudget -G govbudget
COPY --from=builder /app/target/*.jar app.jar
RUN chown govbudget:govbudget app.jar
USER govbudget
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]