FROM fedora:latest
MAINTAINER Erwan Demairy erwan.demairy@inria.fr

ARG JAVA_VERSION=1.8.0
ARG MAVEN_VERSION=3.3.9

RUN dnf update -y 
RUN dnf upgrade -y 
RUN dnf install maven wget git -y 
RUN dnf install -y java-$JAVA_VERSION-openjdk-devel git-core
RUN git clone https://github.com/Wimmics/corese.git
WORKDIR corese/
RUN mvn -Dmaven.test.skip=true package
EXPOSE 8080
ENTRYPOINT java -jar ./kgserver/target/corese-server-3.2.1p-SNAPSHOT-jar-with-dependencies.jar
    
