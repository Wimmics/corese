#!/bin/bash

kill $(lsof -t -i:8085)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8085 -l ../kgtool/target/classes/demographie/regions_departements.ttl &
sleep 10s

kill $(lsof -t -i:8086)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8086 -l ../kgtool/target/classes/demographie/departements_names.ttl &
sleep 10s

kill $(lsof -t -i:8087)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8087 -l ../kgtool/target/classes/demographie/regions_names.ttl &
sleep 10s

kill $(lsof -t -i:8088)
java -jar ../kgserver/target/corese-server-*-jar-with-dependencies.jar -p 8088 -l ../kgtool/target/classes/demographie/popleg-2013-sc.ttl &

