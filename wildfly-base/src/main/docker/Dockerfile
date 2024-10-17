FROM adoptopenjdk/openjdk11:jdk-11.0.24_8-alpine-slim

ARG WILDFLY_VERSION
ENV JBOSS_HOME /opt/jboss/wildfly

USER root
RUN mkdir -p /opt/jboss && adduser -D -h /opt/jboss jboss && apk update && apk add curl && apk add libaio && rm -rf /var/cache/apk/*

USER jboss
WORKDIR /opt/jboss
RUN curl -LO https://github.com/wildfly/wildfly/releases/download/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.zip && unzip wildfly-$WILDFLY_VERSION.zip && mv $HOME/wildfly-$WILDFLY_VERSION $HOME/wildfly && rm wildfly-$WILDFLY_VERSION.zip

ENV TZ=CET

ENV HIBERNATE_MODULE $JBOSS_HOME/modules/system/layers/base/org/hibernate/main
ENV POSTGRES_MODULE $JBOSS_HOME/modules/org/postgresql/main
ENV ARTEMIS_MODULE $JBOSS_HOME/modules/system/layers/base/org/apache/activemq/artemis
ENV PGPASSWORD=postgres

RUN mkdir -p $JBOSS_HOME/modules/org/postgresql/main

COPY postgres/module.xml $POSTGRES_MODULE
COPY dependencies/postgresql-${wildfly.postgres.module.postgres.version}.jar $POSTGRES_MODULE
COPY dependencies/postgis-jdbc-${wildfly.postgres.module.postgis.version}.jar $POSTGRES_MODULE
COPY dependencies/postgis-geometry-${wildfly.postgres.module.postgis.version}.jar $POSTGRES_MODULE

COPY hibernate/module.xml $HIBERNATE_MODULE
COPY dependencies/hibernate-core-${wildfly.hibernate.module.spatial.version}.jar $HIBERNATE_MODULE
COPY dependencies/hibernate-envers-${wildfly.hibernate.module.spatial.version}.jar $HIBERNATE_MODULE
COPY dependencies/hibernate-spatial-${wildfly.hibernate.module.spatial.version}.jar $HIBERNATE_MODULE
COPY dependencies/jts-core-${wildfly.hibernate.module.jts.version}.jar $HIBERNATE_MODULE
COPY dependencies/geolatte-geom-${wildfly.hibernate.module.geolatte.version}.jar $HIBERNATE_MODULE

COPY artemis $ARTEMIS_MODULE
COPY dependencies/artemis-server-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-service-extensions-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-selector-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-jms-server-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-jms-client-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-jdbc-store-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-hqclient-protocol-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-dto-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-core-client-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main
COPY dependencies/artemis-cli-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/main

COPY dependencies/artemis-journal-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/journal/main
COPY dependencies/artemis-commons-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/journal/main

COPY dependencies/artemis-amqp-protocol-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/protocol/amqp/main
COPY dependencies/artemis-hornetq-protocol-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/protocol/hornetq/main
COPY dependencies/artemis-stomp-protocol-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/protocol/stomp/main

COPY dependencies/artemis-ra-${wildfly.artemis.module.artemis.version}.jar $ARTEMIS_MODULE/ra/main

# Standalone config
COPY standalone.conf $JBOSS_HOME/bin/
COPY start.sh /opt/jboss/

# CLI Scripts
COPY cli-scripts $JBOSS_HOME/cli-scripts

USER root
RUN mkdir -p /app/logs && \
    chown -R jboss:jboss /app/logs && \
    chmod 777 /app/logs && \
    mkdir -p /opt/jboss/wildfly/standalone/log/ && \
    chown -R jboss:jboss /opt/jboss/wildfly/standalone/log && \
    chmod 777 /opt/jboss/wildfly/standalone/log/ && \
    chmod 755 /opt/jboss/start.sh && \
    chown -R jboss:jboss /opt/jboss
# install TZDATA, set timesone then delete TZDATA
# alpine linux does not include curl. install it, but delete the apk cache to keep layer size small
RUN echo "@edge http://nl.alpinelinux.org/alpine/edge/main" >> /etc/apk/repositories && \
    apk update && apk add tzdata ttf-dejavu postgresql-client bash && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*

# NPE in sun.awt.FontConfiguration.getVersion(...), see https://github.com/AdoptOpenJDK/openjdk-docker/issues/75
RUN apk add --no-cache fontconfig
RUN ln -s /usr/lib/libfontconfig.so.1 /usr/lib/libfontconfig.so && \
    ln -s /lib/libuuid.so.1 /usr/lib/libuuid.so.1 && \
    ln -s /lib/libc.musl-x86_64.so.1 /usr/lib/libc.musl-x86_64.so.1
ENV LD_LIBRARY_PATH /usr/lib

USER jboss

RUN $JBOSS_HOME/bin/add-user.sh -u 'admin' -p 'Wildfly4ever!'
RUN $JBOSS_HOME/bin/add-user.sh -a 'test' -p 'test' -g guest

RUN $JBOSS_HOME/bin/jboss-cli.sh \
        --file=$JBOSS_HOME/cli-scripts/uvms_configuration.cli \
        --properties=$JBOSS_HOME/cli-scripts/uvms.properties  && \
    $JBOSS_HOME/bin/jboss-cli.sh \
        --file=$JBOSS_HOME/cli-scripts/uvms_datasources.cli \
        --properties=$JBOSS_HOME/cli-scripts/uvms.properties && \
    $JBOSS_HOME/bin/jboss-cli.sh \
        --file=$JBOSS_HOME/cli-scripts/uvms_messaging.cli \
        --properties=$JBOSS_HOME/cli-scripts/uvms.properties

CMD ["/opt/jboss/start.sh", "/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "--debug", "*:8787"]

# Expose stuff to outside world
EXPOSE 8080 9990 5445 5455 8787
VOLUME ["/app/logs", "/opt/jboss/wildfly/standalone/log"]
