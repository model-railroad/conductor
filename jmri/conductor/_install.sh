#!/bin/bash

# Parse properties
JV=$(sed -n -e "/vers_java/s/.*=\(.*\)/\\1/p" gradle.properties)
ARTIFACT=$(sed -n -e "/artifact_vers/s/.*=\(.*\)/\\1/p" gradle.properties)


# Detect JMRI and install method
JDIR=""
if [[ $(uname) =~ CYGWIN_.* || $(uname) =~ MSYS_.* ]]; then
  if [[ $(uname) =~ CYGWIN_.* ]]; then
    JDIR=/cygdrive/c/Program\ Files\ \(x86\)/JMRI
  else
    JDIR=/c/Program\ Files\ \(x86\)/JMRI
  fi

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

if [[ -d "$JDIR" ]]; then
  echo "---- JMRI install directory: $JDIR"
else
  echo "---- JMRI install directory not found. Please adjust $0"
fi

# Detect which version of Java we need
echo
echo "---- Build desired toolchain is Java $JV"

if ! grep -qs "$JV" $(java -version 2>&1) ; then
  if [[ $(uname) =~ CYGWIN_.* || $(uname) =~ MSYS_.* ]]; then
    PF=$(cygpath "$PROGRAMFILES")
    JS=$(find "$PF/Java" -type f -name javac.exe | grep "$JV" | sort -r | head -n 1)
    JS=$(cygpath -w "${JS//\/bin*/}")
  else
    JS=$(ls /usr/lib/jvm/*java8*/bin/javac | head -n 1)
    JS="${JS//\/bin*/}"
  fi
  if [[ -d "$JS" ]]; then
    export JAVA_HOME="$JS"
  else
    echo "---- Consider installing Java $JV and setting JAVA_HOME for it."
  fi
  echo "---- JAVA_HOME = $JAVA_HOME"
fi

GRADLE_CMD="shadowJar"
if [[ "$1" == "--skip-tests" ]]; then
  shift
else
  GRADLE_CMD="$GRADLE_CMD test"
fi

set -e
echo
echo "---- Building with gradle..."
./gradlew $GRADLE_CMD --console=plain $@

echo
echo "---- Copying ${ARTIFACT} files..."
op src/Conductor.py                          jython/Conductor.py
op build/libs/conductor-${ARTIFACT}-all.jar lib/conductor.jar

if [[ ! -d "$JDIR" ]]; then
  echo
  echo "==> NOT COPIED TO JMRI --- Missing $JDIR"
  echo
  exit 1
fi

