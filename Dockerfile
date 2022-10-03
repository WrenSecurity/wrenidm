FROM maven:3.8.6 AS project-build

RUN apt-get update && apt-get install -y unzip

WORKDIR /project
COPY . .

RUN mvn package

RUN WRENIDM_VERSION=$(mvn -Dexpression=project.version -q -DforceStdout help:evaluate) && unzip openidm-zip/target/wrenidm-$WRENIDM_VERSION.zip -d /build

FROM openjdk:17-bullseye

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
