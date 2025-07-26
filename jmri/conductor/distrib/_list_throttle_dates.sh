#!/usr/bin/bash

set -e
cd $(dirname "$0")
pwd
pushd ../engine2k/src/main/resources/v2/script

for F in *.kts; do
  D=$(git log --diff-filter=A --format=%ad --date=short -- "$F")
  T=$(sed -n -e '/^val .*= throttle(/s/val \(.* = th.*([ 0-9]\+)\).*/\1/p'  "$F" | tr "\n" " ")
  echo "$D - $F - $T"
done

popd
pushd ../src/test/resources/v2

for F in *.txt; do
  D=$(git log --diff-filter=A --format=%ad --date=short -- "$F")
  T=$(sed -n -e '/^Throttle.*=/s/.*\(Th.*=[ 0-9]\+\).*/\1/p' "$F" | tr -d "\n")
  echo "$D - $F - $T"
done

