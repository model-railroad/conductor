#!/bin/bash

cd $(dirname "$0")

# Parse properties
JV=$(sed -n -e "/propVersJava/s/.*=\(.*\)/\\1/p" gradle.properties)
ARTIFACT=$(sed -n -e "/propArtifactVers/s/.*=\(.*\)/\\1/p" gradle.properties)

# Detect which version of Java we need
echo
echo "---- Build desired toolchain is Java $JV"

JA="java"
if ! grep -qs "$JV" $($JA -version 2>&1) ; then
  if [[ $(uname) =~ (CYGWIN_|MSYS_|MINGW).* ]]; then
    PF=$(cygpath "$PROGRAMFILES")
    JC=$(find "$PF/Java" -type f -name javac.exe | grep "$JV" | sort -r | head -n 1)
    JS=$(cygpath -w "${JC//\/bin*/}")
    JA=$(cygpath -w "${JC/javac/java}")
    JE=$(cygpath "$JA")
  else
    JC=$(ls /usr/lib/jvm/*java*$JV*/bin/javac | head -n 1)
    JS="${JS//\/bin*/}"
    JA="${JC/javac/java}"
    JE="$JA"
  fi
  if [[ -d "$JS" ]]; then
    export JAVA_HOME="$JS"
  else
    echo "---- Consider installing Java $JV and setting JAVA_HOME for it."
  fi
  echo "---- JAVA_HOME = $JAVA_HOME"
fi

GRADLE_CMD="shadowJar"

set -e
echo
echo "---- Building with gradle..."
./gradlew $GRADLE_CMD --console=plain

echo
echo "---- Running ${ARTIFACT}..."
echo

# deactivate output color on some term which do not support it
case "$TERM" in
  cygwin)
    export NO_COLOR=1
    ;;
esac

set -x
"$JE" -jar ./build/libs/DazzServ-${ARTIFACT}-all.jar $@
