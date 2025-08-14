#!/bin/bash

cd $(dirname "$0")

# Parse properties
JV=$(sed -n -e "/propVersJava/s/.*=\(.*\)/\\1/p" gradle.properties)
ARTIFACT=$(sed -n -e "/propArtifactVers/s/.*=\(.*\)/\\1/p" gradle.properties)

# Detect which version of Java we need
echo
echo "---- Build desired toolchain is Java $JV"

if ! grep -qs "$JV" $(java -version 2>&1) ; then
  if [[ $(uname) =~ (CYGWIN_|MSYS_|MINGW).* ]]; then
    PF=$(cygpath "$PROGRAMFILES")
    JS=$(find "$PF/Java" -type f -name javac.exe | grep "$JV" | sort -r | head -n 1)
    JS=$(cygpath -w "${JS//\/bin*/}")
  else
    JS=$(ls /usr/lib/jvm/*java*$JV*/bin/javac | head -n 1)
    JS="${JS//\/bin*/}"
  fi
  if [[ -d "$JS" ]]; then
    export JAVA_HOME="$JS"
  else
    echo "---- Consider installing Java $JV and setting JAVA_HOME for it."
  fi
  echo "---- JAVA_HOME = $JAVA_HOME"
fi

GRADLE_CMD="assembleDist shadowJar"
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
echo "---- Result ${ARTIFACT} files..."
ls -lah build/distributions/DazzServ-${ARTIFACT}.* build/libs/DazzServ-${ARTIFACT}-all.jar
unzip -p build/libs/DazzServ-${ARTIFACT}-all.jar META-INF/MANIFEST.MF
