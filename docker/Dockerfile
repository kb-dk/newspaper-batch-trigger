FROM openjdk:7-jre

ENV component="newspaper-batch-trigger-1.9-SNAPSHOT"

WORKDIR /opt/

#Copy component to docker container and extract
ADD ${component}-package.tar.gz .

WORKDIR /opt/${component}

COPY trigger-config.sh logback.xml /conf/

ENTRYPOINT [ "/opt/${component}/bin/trigger-on-new-batch.sh", "/conf/trigger-config.sh",]


