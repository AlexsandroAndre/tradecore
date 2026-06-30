FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline -B -q 2>/dev/null || mvn dependency:go-offline -B -q

COPY src src

RUN ./mvnw clean package -B -DskipTests -q 2>/dev/null || mvn clean package -B -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar application.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication -XX:-UseAdaptiveSizePolicy"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar application.jar"]
