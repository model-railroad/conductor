#!/bin/bash
if [[ $(uname) =~ CYGWIN_.* ]]; then
    set -x
    cp src/Conductor.py /cygdrive/c/Program\ Files\ \(x86\)/JMRI/jython/Conductor.py
    ./gradlew build
    cp build/libs/conductor-1.0-SNAPSHOT-all.jar /cygdrive/c/Program\ Files\ \(x86\)/JMRI/lib/conductor.jar
else
    echo "Invalid script. Please adjust $0"
fi
