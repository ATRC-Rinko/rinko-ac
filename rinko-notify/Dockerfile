FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw package -DskipTests -pl rinko-notify -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG SKYWALKING_AGENT=
ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED"
COPY --from=builder /app/rinko-notify/target/*.jar app.jar
RUN if [ -n "$SKYWALKING_AGENT" ] && [ -f "$SKYWALKING_AGENT" ]; then cp "$SKYWALKING_AGENT" skywalking-agent.jar; fi
EXPOSE 8084
ENTRYPOINT if [ -f skywalking-agent.jar ]; then \
    java $JAVA_OPTS -javaagent:skywalking-agent.jar -jar app.jar; \
  else \
    java $JAVA_OPTS -jar app.jar; \
  fi
