#!/bin/bash
jacocoArgLine=$1
buildPath=$2
resourcePath=$3
java ${jacocoArgLine} -jar ${buildPath}/corese-server-*-jar-with-dependencies.jar -lh -l ${resourcePath} -ssl -jks corese.inria.fr.jks -pwd coreseatwimmics -pssl 8443 &
echo $! > SSL_SERVER_PID 
