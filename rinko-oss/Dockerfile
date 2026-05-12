FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests -pl rinko-oss -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
COPY --from=builder /app/rinko-oss/target/*.jar app.jar
COPY skywalking-agent/skywalking-agent.jar skywalking-agent.jar
EXPOSE 8082
ENTRYPOINT java $JAVA_OPTS -javaagent:skywalking-agent.jar -jar app.jar
