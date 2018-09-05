#!/bin/bash
jacocoArgLine=$1
buildPath=$2
resourcePath=$3
COMMAND="java ${jacocoArgLine} -Dfile.encoding=UTF-8 -jar ${buildPath}/corese-server-*-jar-with-dependencies.jar -l ${resourcePath} -ssl -jks corese.inria.fr.jks -pwd coreseatwimmics  -p 8080 -ssl -pssl 8443 &"
echo "Launching ${COMMAND}"
eval $COMMAND 2>&1 > /tmp/corese.log
sleep 5
echo $! > SSL_SERVER_PID
