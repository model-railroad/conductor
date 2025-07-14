#!/usr/bin/bash
EXPECT=24
NODE_MAJ=$(node --version | tr -d v | cut -d . -f 1)

if [[ "NODE_MAJ" -lt "$EXPECT" ]]; then
  echo "Expecting node.js version v$EXPECT or higher, but was $NODE_MAJ."
  exit 1
fi

# Git branch that works with my older 2.21 git-bash-win
# Starting with 2.22, I have "git branch --show-current"
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

GIT_HASH=$(git log --format="%h"  -n 1 HEAD)
GIT_LONG=$(git log --format="%ci" -n 1 HEAD)
VERSION_MAJOR=$(grep VERSION_MAJOR src/GitBuild.ts | cut -d \" -f 2)
VERSION_MINOR=$(grep VERSION_MINOR src/GitBuild.ts | cut -d \" -f 2)

cat <<EOL >src/GitBuild.ts
export const GIT_BRANCH_STR = "$GIT_BRANCH";
export const GIT_HASH_STR = "$GIT_HASH";
export const GIT_LONG_STR = "Build $GIT_BRANCH @ $GIT_HASH, $GIT_LONG";
export const VERSION_MAJOR = "$VERSION_MAJOR";
export const VERSION_MINOR = "$VERSION_MINOR";
EOL

echo "GIT HASH: $GIT_LONG"
echo -n "Build started at " ; date
npm run build
echo -n "Build ended at " ; date
echo
cat src/GitBuild.ts
echo

#~~

