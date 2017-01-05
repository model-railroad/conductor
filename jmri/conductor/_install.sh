#!/bin/bash
JDIR=""
if [[ $(uname) =~ CYGWIN_.* ]]; then
    JDIR=/cygdrive/c/Program\ Files\ \(x86\)/JMRI

    function op() {
        cp -v "$1" "$JDIR"/"$2"
    }
else
    JDIR=~/bin/JMRI
    
    function op() {
        ln -sfv "$PWD"/"$1" "$JDIR"/"$2"
    } 
fi

if [[ ! -d "$JDIR" ]]; then
    echo "Invalid script. Please adjust $0"
fi

set -x
set -e
./gradlew build 
op src/Conductor.py                          jython/Conductor.py
op build/libs/conductor-1.0-SNAPSHOT-all.jar lib/conductor.jar
