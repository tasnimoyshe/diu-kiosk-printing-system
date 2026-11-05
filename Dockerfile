# =========================================================
# STAGE 1 — BUILD REACT FRONTEND
# =========================================================

FROM node:24-bookworm-slim AS frontend-build

WORKDIR /workspace/frontend

COPY frontend/package*.json ./

RUN if [ -f package-lock.json ]; then \
        npm ci; \
    else \
        npm install; \
    fi

COPY frontend/ ./

RUN npm run build


# =========================================================
# STAGE 2 — BUILD SPRING BOOT BACKEND
# =========================================================

FROM eclipse-temurin:17-jdk-jammy AS backend-build

WORKDIR /workspace/backend

COPY backend/.mvn .mvn
COPY backend/mvnw .
COPY backend/pom.xml .

RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B -DskipTests dependency:go-offline

COPY backend/src ./src

# Always package the newly generated React production build.
RUN rm -rf src/main/resources/static \
    && mkdir -p src/main/resources/static

COPY --from=frontend-build \
    /workspace/frontend/dist/ \
    ./src/main/resources/static/

RUN ./mvnw -B -DskipTests clean package


# =========================================================
# STAGE 3 — RUNTIME
# =========================================================

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive \
       apt-get install -y --no-install-recommends \
       libreoffice-writer \
       fontconfig \
       fonts-dejavu-core \
       fonts-liberation \
    && rm -rf /var/lib/apt/lists/*


# =========================================================
# NON-ROOT APPLICATION USER
# =========================================================

RUN useradd \
    --create-home \
    --uid 10001 \
    kiosk

WORKDIR /app


# =========================================================
# COPY SPRING BOOT APPLICATION
# =========================================================

COPY --from=backend-build \
    --chown=kiosk:kiosk \
    /workspace/backend/target/*.jar \
    /app/app.jar


# =========================================================
# LIBREOFFICE
# =========================================================

ENV SOFFICE_PATH=/usr/bin/soffice


# =========================================================
# RENDER FREE — 512 MB JVM MEMORY CONFIGURATION
# =========================================================
#
# Previous deployment:
#
#   Heap max:       220 MB
#   Metaspace max:   96 MB
#
# The application successfully started, but crashed when
# additional lazy Spring/Hibernate classes were loaded:
#
#   java.lang.OutOfMemoryError: Metaspace
#
# Revised allocation:
#
#   Heap max:       200 MB
#   Metaspace max:  144 MB
#   Code cache:      24 MB
#   Direct memory:   16 MB
#   Thread stacks:  512 KB
#
# This provides substantially more class metadata capacity
# while keeping the JVM bounded for Render's 512 MB instance.
#
# SerialGC is intentionally used because this application runs
# on one very small CPU/RAM instance and SerialGC has low native
# memory overhead.
#
# Class unloading is enabled so unused dynamically generated
# classes/proxies can be reclaimed during full collections.
#
# =========================================================

ENV JAVA_TOOL_OPTIONS="-Xms32m -Xmx200m -XX:MaxMetaspaceSize=144m -XX:ReservedCodeCacheSize=24m -XX:MaxDirectMemorySize=16m -Xss512k -XX:+UseSerialGC -XX:+ClassUnloading -XX:MinMetaspaceFreeRatio=10 -XX:MaxMetaspaceFreeRatio=20 -XX:+ExitOnOutOfMemoryError -Djava.awt.headless=true"


# Reduce native allocator fragmentation inside the Linux
# container.
ENV MALLOC_ARENA_MAX=2


# =========================================================
# APPLICATION USER
# =========================================================

USER kiosk


# =========================================================
# RENDER WEB SERVICE PORT
# =========================================================

EXPOSE 10000


# =========================================================
# START APPLICATION
# =========================================================

ENTRYPOINT ["java", "-jar", "/app/app.jar"]