#!/bin/bash

kill $(lsof -t -i:8083)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8083 -l ../kgtool/target/classes/demographie/globalBGPS2.ttl &
sleep 10s

kill $(lsof -t -i:8084)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8084 -l ../kgtool/target/classes/demographie/globalBGPS1.ttl &
sleep 10s

kill $(lsof -t -i:8088)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8088 -l ../kgtool/target/classes/demographie/popleg-2013-sc.ttl &

