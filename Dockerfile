ARG JAVA_VERSION=8

FROM openjdk:$JAVA_VERSION

ARG WRENIDM_VERSION=6.0.0-SNAPSHOT

# Install dependencies
RUN apt-get install -y unzip

# Create wrenidm user
ARG WRENIDM_UID=1000
ARG WRENIDM_GID=1000
RUN addgroup --gid ${WRENIDM_GID} wrenidm && \
    adduser --uid ${WRENIDM_UID} --gid ${WRENIDM_GID} --system wrenidm

# Deploy wrenidm project
ADD openidm-zip/target/wrenidm-${WRENIDM_VERSION}.zip /tmp
RUN unzip /tmp/wrenidm-${WRENIDM_VERSION}.zip -d /opt && chown -R wrenidm /opt/wrenidm && rm /tmp/wrenidm-${WRENIDM_VERSION}.zip

USER ${WRENIDM_UID}
WORKDIR /opt/wrenidm

VOLUME /opt/wrenidm/logs

ENTRYPOINT ["/opt/wrenidm/startup.sh"]
