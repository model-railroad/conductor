#!/bin/bash

# Parse properties
JV=$(sed -n -e "/vers_java/s/.*=\(.*\)/\\1/p" gradle.properties)
ARTIFACT=$(sed -n -e "/artifact_vers/s/.*=\(.*\)/\\1/p" gradle.properties)


# Detect which version of Java we need
echo
echo "---- Build desired toolchain is Java $JV"

if ! grep -qs "$JV" $(java -version 2>&1) ; then
  if [[ $(uname) =~ CYGWIN_.* || $(uname) =~ MSYS_.* ]]; then
    PF=$(cygpath "$PROGRAMFILES")
    JS=$(find "$PF/Java" -type f -name javac.exe | grep "$JV" | sort -r | head -n 1)
    JV="${JS/javac/java}"
    JS=$(cygpath -w "${JS//\/bin*/}")
  else
    JS=$(ls /usr/lib/jvm/*java*$JV*/bin/javac | head -n 1)
    JV="${JS/javac/java}"
    JS="${JS//\/bin*/}"
  fi
  if [[ -d "$JS" ]]; then
    export JAVA_HOME="$JS"
  else
    echo "---- Consider installing Java $JV and setting JAVA_HOME for it."
  fi
  echo "---- JAVA_HOME = $JAVA_HOME"
fi

set -x
./gradlew shadowJar
"$JV" -cp build/libs/conductor-${ARTIFACT}-all.jar com.alflabs.conductor.v2.DevEntryPoint2
