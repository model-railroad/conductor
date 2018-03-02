#!/bin/bash
cd $(dirname "$0")
cd ..
./gradlew assembleDebug
cp -v ./app/build/outputs/apk/debug/app-debug.apk distrib/rtac-debug.apk
