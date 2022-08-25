#!/bin/bash
set -e
cd $(dirname "$0")
cd .

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
  $DRY git submodule add git@bitbucket.org:$GIT_USER/libutils.git $ROOT/LibUtils
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
  $DRY git submodule add git@github.com:ralfoide/androidsvg.git $ROOT/androidsvg
fi
$DRY git submodule update --init $ROOT/androidsvg
