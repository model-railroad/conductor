#!/bin/bash
JDIR=""
if [[ $(uname) =~ CYGWIN_.* || $(uname) =~ MSYS_.* ]]; then
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
  echo "## JMRI install directory not found. Please adjust $0"
fi

# We currently need Java 1.8
if ! grep -qs "1.8" $(java -version 2>&1) ; then
  if [[ $(uname) =~ CYGWIN_.* || $(uname) =~ MSYS_.* ]]; then
    PF=$(cygpath "$PROGRAMFILES")
    JS=$(find "$PF/Java" -type f -name javac.exe | grep 1.8 | sort -r | head -n 1)
    JS=$(cygpath -w "${JS//\/bin*/}")
  else
    JS=$(ls /usr/lib/jvm/*java8*/bin/javac | head -n 1)
    JS="${JS//\/bin*/}"
  fi
  if [[ -d "$JS" ]]; then
    export JAVA_HOME="$JS"
  else
    echo "## Consider installing Java 1.8 and setting JAVA_HOME for it."
  fi
  echo "## JAVA_HOME = $JAVA_HOME"
fi


GRADLE_CMD="build"
if [[ "$1" == "--skip-tests" ]]; then
    GRADLE_CMD="fatJar"
    shift
fi

set -e
echo; echo "---- Building with gradle..."
./gradlew $GRADLE_CMD --console=plain $@
echo; echo "---- Copying files..."
op src/Conductor.py                          jython/Conductor.py
op build/libs/conductor-2.1-SNAPSHOT-all.jar lib/conductor.jar

if [[ ! -d "$JDIR" ]]; then echo; echo "==> NOT COPIED TO JMRI --- Missing $JDIR"; echo; exit 1; fi
