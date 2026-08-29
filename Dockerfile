# =============================================================================
# Shared multi-stage build for every krishna.shop Spring Boot service.
#
# Parameterised by MODULE (the Maven module / directory name, e.g. user-service).
# docker-compose passes it as a build arg so a single Dockerfile builds them all.
#
#   docker build --build-arg MODULE=user-service -t krishna/user-service .
# =============================================================================

# ---- Stage 1: build the reactor (module + its dependencies, e.g. common-lib) ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the whole source tree. .dockerignore keeps target/ and cruft out.
COPY . .

ARG MODULE
# -pl <module> -am  => build this module *and* the modules it depends on (common-lib).
# The BuildKit cache mount keeps the ~/.m2 repo warm across builds.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl ${MODULE} -am clean package -DskipTests

# Normalise the produced fat jar to a predictable name for the runtime stage.
RUN cp ${MODULE}/target/*.jar /build/app.jar

# ---- Stage 2: slim runtime ----
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Run as an unprivileged user.
RUN useradd -r -u 1001 -g root appuser
COPY --from=build /build/app.jar app.jar
USER appuser

# Container-aware heap sizing; override JAVA_OPTS per service if needed.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
