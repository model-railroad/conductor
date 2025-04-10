#!/bin/bash
set -e
cd $(dirname "$0")
cd .

REPO_ORG="ralfoide"
DRY=echo
if [[ "$1" == "-f" ]]; then
	DRY=""
	set -x
else
	echo ; echo "#### DRY-RUN MODE. Use -f to run. ####" ; echo
fi

ROOT=.
if [[ $(git --version) =~ 1.[1-7] ]]; then
    # Note: On old git 1.7, "git submodule" needs to be run from the "toplevel"
    # where the .git directory is located.
    cd ../..
    ROOT=android/RTAC
    echo "### Warning: git 1.7 detected, switching to ROOT=$ROOT and PWD=$PWD" ; echo
fi

GIT_USER=$(sed -n '/email = /s/.*= \(.*\)@.*/\1/p' ~/.gitconfig)
if [[ -z $GIT_USER ]]; then set +x; echo "Git user not found"; exit 1; fi

if [[ ! -d $ROOT/LibUtils ]]; then
  if [[ "$GIT_USER" == "$REPO_ORG"]]; then
    $DRY git submodule add "git@github.com:$GIT_USER/libutils.git" "$ROOT/LibUtils"
  else
    $DRY git submodule add "https://github.com/$REPO_ORG/libutils.git" "$ROOT/LibUtils"
  fi
fi

$DRY git submodule update --init $ROOT/LibUtils
LIB_BRANCH="android-lib-v3"
(   $DRY cd $ROOT/LibUtils
    if ! git branch | grep -q $LIB_BRANCH ; then
        $DRY git branch --track $LIB_BRANCH origin/$LIB_BRANCH
        $DRY git checkout $LIB_BRANCH
    fi
)

if [[ ! -d $ROOT/androidsvg ]]; then
  if [[ "$GIT_USER" == "$REPO_ORG"]]; then
    $DRY git submodule add "git@github.com:$GIT_USER/androidsvg.git" "$ROOT/androidsvg"
  else
    $DRY git submodule add "https://github.com/$REPO_ORG/androidsvg.git" "$ROOT/androidsvg"
  fi
fi
$DRY git submodule update --init $ROOT/androidsvg
