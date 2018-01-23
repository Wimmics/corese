FROM centos:latest
MAINTAINER gom "gomgom68@gmail.com"

ARG JAVA_VERSION=1.8.0
ARG MAVEN_VERSION=3.3.9

RUN yum update -y 
RUN yum upgrade -y 
RUN yum install maven wget git -y 
RUN yum install -y java-$JAVA_VERSION-openjdk-devel git-core
RUN git clone https://github.com/Ahmedko/corese.git
WORKDIR corese/
RUN mvn -Dmaven.test.skip=true package; exit 0
EXPOSE 8080
ENTRYPOINT java -jar ./kgserver/target/corese-server-3.2.1p-SNAPSHOT-jar-with-dependencies.jar
