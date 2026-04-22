FROM maven:3.9.14-eclipse-temurin-21-alpine AS Build-Stage
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S streambase && adduser -S streambase -G streambase
USER streambase:streambase
COPY --from=Build-Stage /app/target/*.jar streambase.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/streambase.jar"]