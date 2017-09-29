#!/usr/bin/env bash
COMMAND="mvn \"-Dexec.args=-classpath %classpath fr.inria.corese.draw.gnuplot.GnuplotDrawer $*\" -Dexec.executable=${JAVA_HOME}/bin/java org.codehaus.mojo:exec-maven-plugin:1.2.1:exec"
echo $COMMAND
cd $(dirname $0) && eval $COMMAND
