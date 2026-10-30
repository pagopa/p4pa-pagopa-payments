# syntax=docker/dockerfile:1.4@sha256:9ba7531bd80fb0a858632727cf7a112fbfd19b17e94c4e84ced81e24ef1a0dbc

#
# 🎯 Version Management
#
ARG IMAGE="public.ecr.aws/docker/library/eclipse-temurin"
ARG IMAGE_VERSION="21-alpine-3.24"
ARG IMAGE_SHA="6ea5548706b60ac0a602eaf48af74792cbab012d90e811ca8db6184b16b5c3d6"
ARG APPINSIGHTS_VERSION="3.7.9"

# 🌍 Timezone Configuration
ARG TZ="Europe/Rome"

# 🔧 Build Configuration
ARG GRADLE_OPTS="-Dorg.gradle.daemon=false \
    -Dorg.gradle.parallel=true \
    -Dorg.gradle.caching=true \
    -Dorg.gradle.configureondemand=true \
    -Dorg.gradle.jvmargs=-Xmx2g"

# 👤 App Configuration
ARG APP_USER="appuser"
ARG APP_GROUP="appgroup"
ARG APP_HOME="/app"

#
# 📥 Base Setup Stage
#
FROM ${IMAGE}@sha256:${IMAGE_SHA} AS base
ARG APP_USER
ARG APP_GROUP

# Install base packages
RUN apk add --no-cache \
    wget \
    unzip \
    bash \
    shadow \
    git

# Create user
RUN groupadd --system --gid 1000 ${APP_GROUP} && \
    useradd --system --gid ${APP_GROUP} --uid 1000 --shell /bin/bash --create-home ${APP_USER}

#
# 📦 Gradle Setup Stage
#
FROM base AS gradle-setup
ARG GRADLE_OPTS
ARG APP_USER
ARG APP_GROUP

# Set environment variables for Gradle
ENV GRADLE_OPTS="${GRADLE_OPTS}"

WORKDIR /build

COPY --chown=${APP_USER}:${APP_GROUP} gradle gradle/
COPY --chown=${APP_USER}:${APP_GROUP} gradlew gradlew

# Download and verify Gradle with progress bar
RUN ./gradlew --version

#
# 📚 Dependencies Stage
#
FROM gradle-setup AS dependencies
ARG APP_USER
ARG APP_GROUP

WORKDIR /build

# Copy build configuration
COPY --chown=${APP_USER}:${APP_GROUP} build.gradle.kts settings.gradle.kts ./
COPY --chown=${APP_USER}:${APP_GROUP} gradle.lockfile ./
COPY --chown=${APP_USER}:${APP_GROUP} openapi openapi/
COPY --chown=${APP_USER}:${APP_GROUP} src/main/resources/soap src/main/resources/soap/
COPY .git .git

# Generate OpenAPI stubs and download dependencies
RUN mkdir -p src/main/java && \
    chown -R ${APP_USER}:${APP_GROUP} /build && \
    chmod -R 775 /build

USER ${APP_USER}

RUN ./gradlew dependenciesBuild dependencies --no-daemon >/dev/null

#
# 🏗️ Build Stage
#
FROM dependencies AS build
ARG APP_USER
ARG APP_GROUP

# Copy source code
COPY --chown=${APP_USER}:${APP_GROUP} src src/

# Build application
RUN ./gradlew bootJar --no-daemon

#
# 🚀 Runtime Stage
#
FROM ${IMAGE}@sha256:${IMAGE_SHA} AS runtime-base
ARG APP_USER
ARG APP_GROUP
ARG APP_HOME
ARG APPINSIGHTS_VERSION
ARG TZ

WORKDIR ${APP_HOME}

# Set timezone environment variable
ENV TZ=${TZ}

# 🛡️ Security Setup and Timezone
RUN apk upgrade --no-cache && \
    apk add --no-cache \
        tini \
        curl \
        # Configure timezone + ENV=TZ \
        tzdata && \
    # Create user and group \
    addgroup -S ${APP_GROUP} && \
    adduser -S ${APP_USER} -G ${APP_GROUP}

ADD --chmod=644 https://github.com/microsoft/ApplicationInsights-Java/releases/download/${APPINSIGHTS_VERSION}/applicationinsights-agent-${APPINSIGHTS_VERSION}.jar ${APP_HOME}/applicationinsights-agent.jar

FROM runtime-base AS runtime
ARG APP_USER
ARG APP_GROUP
ARG APP_HOME

# 📦 Copy Artifacts
COPY --from=build /build/build/libs/*.jar ${APP_HOME}/app.jar

# 📝 Set Permissions
RUN chown -R ${APP_USER}:${APP_GROUP} ${APP_HOME}

# 🔌 Container Configuration
EXPOSE 8080
USER ${APP_USER}

# 🎬 Startup Configuration
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "/app/app.jar"]
