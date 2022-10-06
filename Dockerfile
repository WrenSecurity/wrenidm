FROM maven:3.8.6-eclipse-temurin-17 AS project-build

RUN apt-get update

# Install management tools
RUN apt-get install -y unzip

# Install headless Chrome dependencies
RUN apt-get install -y ca-certificates fonts-liberation libappindicator3-1 libasound2 libatk-bridge2.0-0 libatk1.0-0 \
    libc6 libcairo2 libcups2 libdbus-1-3 libexpat1 libfontconfig1 libgbm1 libgcc1 libglib2.0-0 libgtk-3-0 libnspr4 \
    libnss3 libpango-1.0-0 libpangocairo-1.0-0 libstdc++6 libx11-6 libx11-xcb1 libxcb1 libxshmfence1 libxcomposite1 libxcursor1 \
    libxdamage1 libxext6 libxfixes3 libxi6 libxrandr2 libxrender1 libxss1 libxtst6 libxcb-dri3-0 lsb-release wget xdg-utils \
    --no-install-recommends

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
