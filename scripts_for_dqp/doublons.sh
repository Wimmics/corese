#!/bin/bash

kill $(lsof -t -i:8081)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8081 -l ../kgtool/target/classes/demographie/cog-2014.ttl &
sleep 10s

kill $(lsof -t -i:8082)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8082 -l ../kgtool/target/classes/demographie/cog-2014.ttl &
sleep 10s

kill $(lsof -t -i:8088)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8088 -l ../kgtool/target/classes/demographie/popleg-2013-sc.ttl &

