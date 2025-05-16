FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY service-registry/pom.xml service-registry/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY chat-service/pom.xml chat-service/
COPY hrms-service/pom.xml hrms-service/

# Build all dependencies first to optimize Docker layer caching
RUN ./mvnw dependency:go-offline -B

COPY service-registry/src service-registry/src
COPY api-gateway/src api-gateway/src
COPY auth-service/src auth-service/src
COPY chat-service/src chat-service/src
COPY hrms-service/src hrms-service/src

RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=/workspace/app/*/target/*.jar
COPY --from=build ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"] 