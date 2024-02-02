#!/bin/bash

CORESE=/usr/local/corese
JAR=$CORESE/corese-server-4.5.0.jar
PROFILE=$CORESE/config/corese-profile.ttl
PROPERTIES=$CORESE/config/corese-properties.properties
OPTIONS=${OPTIONS:-}

LOG4J=$CORESE/config/log4j2.xml
DATA=$CORESE/data
LOG=$CORESE/log/corese-server.log

mkdir -p $DATA $CORESE/log $CORESE/config

# Generate the instructions for loading all RDF files from folder "data"
# Supported extensions: .ttl .jsonld .rdf .csv .tsv .html (for rdfa)
# Parameters:
#   $1: absolute path where to look for files
function genLoadData() {
    path=$1
    cd $path
    echo "Looking for data files in: $path" >>$LOG
    for file in $(ls *); do
        echo "  [ a sw:Load; sw:path <$path/$file> ]" >>$PROFILE
    done
}

echo "======================================================================" >>$LOG

# Check existing log4j properties file or create a new one
if [ -f "$LOG4J" ]; then
    echo "Using user-defined log4j properties file." >>$LOG
else
    echo "Creating new log4j properties file." >>$LOG
    cp $CORESE/log4j2.xml $LOG4J
fi

# Check if JVM heap space if given in the env
if [ -z "$JVM_XMX" ]; then
    XMX=
else
    XMX=-Xmx${JVM_XMX}
fi
echo "JVM heap space option: $XMX" >>$LOG

# Check existing profile or create a new one
if [ -f "$PROFILE" ]; then
    echo "Using user-defined profile." >>$LOG
else
    # Prepare the Corese profile for loading "data/*"
    echo "Creating new profile." >>$LOG
    cat $CORESE/corese-default-profile.ttl >$PROFILE
    echo "st:loadcontent a sw:Workflow; sw:body (" >>$PROFILE
    genLoadData "$DATA"
    echo ').' >>$PROFILE
    echo '' >>$PROFILE
fi
echo "Corese profile:" >>$LOG
cat $PROFILE >>$LOG

# Check existing properties file or create a new one
if [ -f "$PROPERTIES" ]; then
    echo "Using user-defined properties file." >>$LOG
else
    echo "Creating new properties file." >>$LOG
    cp $CORESE/corese-default-properties.properties $PROPERTIES
fi

# Start Corese with the profile
cd $CORESE
java \
    $XMX \
    -Dfile.encoding="UTF-8" \
    -Dlog4j.configurationFile=file:$LOG4J \
    -jar $JAR \
    -p 8080 \
    -lp \
    -pp file://$PROFILE \
    -init $PROPERTIES \
    $OPTIONS
