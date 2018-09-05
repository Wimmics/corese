FROM fedora:28
MAINTAINER Erwan Demairy erwan.demairy@inria.fr

ARG JAVA_VERSION=11.0
ARG MAVEN_VERSION=3.5.3
ARG JAVA_HOME=/usr/lib/jvm/java-10/

RUN dnf update -y 
RUN dnf upgrade -y 
RUN dnf install maven wget git -y 
RUN dnf install -y java-openjdk-devel git-core
RUN /usr/bin/java -version
RUN echo 2 | update-alternatives --config java
RUN /usr/bin/java --version
RUN git clone https://github.com/Wimmics/corese.git -b feature/clean_hardcoded_paths
WORKDIR corese/
RUN mvn -Dmaven.test.skip=true package
EXPOSE 8080
EXPOSE 8443
ENTRYPOINT java -jar ./corese-server/target/corese-server-*-SNAPSHOT-jar-with-dependencies.jar -ssl -jks corese.inria.fr.jks -pwd coreseatwimmics  -p 8080 -ssl -pssl 8443
    
