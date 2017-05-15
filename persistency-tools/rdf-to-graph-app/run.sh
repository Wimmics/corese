#!/usr/bin/env bash
EXPECTED_ARGS=3
E_BADARGS=101

if [ $# -ne $EXPECTED_ARGS ]; then
	echo "Usage: `basename $0` {input_regexp} {database_path} {database_driver}"
	echo "Note: input_regexp follows the regexp syntax of Java"
	echo "Note: database_driver must be chosen among available drivers (neo4j, titandb, etc}"
	exit $E_BADARGS
fi

echo "mvn -Dexec.args=-classpath %classpath fr.inria.wimmics.rdf_to_graph.app.App $1 $2 $3 -Dexec.executable=/usr/bin/java org.codehaus.mojo:exec-maven-plugin:1.2.1:exec"
mvn "-Dexec.args=-classpath %classpath fr.inria.wimmics.rdf_to_graph.app.App $1 $2 $3" -Dexec.executable=/usr/bin/java org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
