#!/bin/bash
JDIR=""
if [[ $(uname) =~ CYGWIN_.* ]]; then
  JDIR=/cygdrive/c/Program\ Files\ \(x86\)/JMRI

  function op() {
    cp -v "$1" distrib/
    if [[ -d "$JDIR" ]]; then
      cp -v "$1" "$JDIR"/"$2"
    fi
  }
else
  JDIR=~/bin/JMRI
    
  function op() {
		cp -v "$1" distrib/
		if [[ -d "$JDIR" ]]; then
			ln -sfv "$PWD"/"$1" "$JDIR"/"$2"
		fi
  }
fi

if [[ ! -d "$JDIR" ]]; then
  echo "Invalid script. Please adjust $0"
fi

set -e
echo; echo "---- Building with gradle..."
./gradlew build --console=plain $@
echo; echo "---- Copying files..."
op src/Conductor.py                          jython/Conductor.py
op build/libs/conductor-0.2-SNAPSHOT-all.jar lib/conductor.jar

if [[ ! -d "$JDIR" ]]; then echo; echo "==> NOT COPIED TO JMRI --- Missing $JDIR"; echo; exit 1; fi
