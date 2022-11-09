FROM maven:3.8.6-eclipse-temurin-17-alpine AS project-build

# Install management tools
RUN apk add unzip

# Install and configure headless Chrome for Puppeteer
RUN apk add chromium
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium-browser
# Use '--no-sandbox' option for Puppeteer's Chromium because of incompatibility with Docker
ENV DISABLE_PUPPETEER_SANDBOX=true

# Copy project files
WORKDIR /project
COPY . .

# Perform actual Wren:IDM build
RUN --mount=type=cache,target=/root/.m2 mvn clean package

# Extract built project into target directory
RUN WRENIDM_VERSION=$(mvn -Dexpression=project.version -q -DforceStdout help:evaluate) && unzip openidm-zip/target/wrenidm-$WRENIDM_VERSION.zip -d /build


FROM eclipse-temurin:17-jdk

# Create wrenidm user
ARG WRENIDM_UID=1000
ARG WRENIDM_GID=1000
RUN addgroup --gid ${WRENIDM_GID} wrenidm && \
    adduser --uid ${WRENIDM_UID} --gid ${WRENIDM_GID} --system wrenidm

# Deploy wrenidm project
COPY --chown=wrenidm:root --from=project-build /build/wrenidm /opt/wrenidm

USER ${WRENIDM_UID}
WORKDIR /opt/wrenidm

VOLUME /opt/wrenidm/logs

ENTRYPOINT ["/opt/wrenidm/startup.sh"]
