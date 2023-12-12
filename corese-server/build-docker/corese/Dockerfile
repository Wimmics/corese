FROM openjdk:11.0.7-jdk-buster

RUN apt-get update -y
RUN apt-get install -y wget

ENV CORESE="/usr/local/corese"
RUN mkdir -p $CORESE
WORKDIR $CORESE

RUN wget http://files.inria.fr/corese/distrib/corese-server-4.5.0.jar

COPY log4j2.xml $CORESE/log4j2.xml
COPY corese-default-profile.ttl $CORESE/corese-default-profile.ttl
COPY corese-default-properties.properties $CORESE/corese-default-properties.properties

ENV CORESESH=$CORESE/corese-server.sh
COPY corese-server.sh $CORESESH
RUN chmod 755 $CORESESH

CMD $CORESESH

