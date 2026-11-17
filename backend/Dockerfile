# syntax=docker/dockerfile:1
# Build context: repository root (see docker-compose.yml), so that the shared
# content/profile.json can be bundled into the WAR.

# ---------- Stage 1: build the WAR ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace/backend

COPY backend/pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:resolve dependency:resolve-plugins || true

COPY content/ /workspace/content/
COPY backend/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package

# ---------- Stage 2: Open Liberty runtime ----------
FROM icr.io/appcafe/open-liberty:kernel-slim-java21-openj9-ubi-minimal

COPY --chown=1001:0 backend/src/main/liberty/config/server.xml /config/server.xml
# Installs only the features listed in server.xml
RUN features.sh

COPY --chown=1001:0 --from=build \
     /workspace/backend/target/liberty/wlp/usr/shared/resources/jdbc/postgresql.jar \
     /opt/ol/wlp/usr/shared/resources/jdbc/postgresql.jar
COPY --chown=1001:0 --from=build /workspace/backend/target/portfolio.war /config/apps/portfolio.war
COPY --chown=1001:0 --chmod=755 backend/docker/healthcheck.sh /opt/healthcheck.sh

RUN configure.sh

EXPOSE 9080
HEALTHCHECK --interval=10s --timeout=6s --start-period=90s --retries=12 CMD ["/opt/healthcheck.sh"]
