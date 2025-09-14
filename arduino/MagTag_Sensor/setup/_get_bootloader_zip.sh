#!/usr/bin/bash
set -e
set -x
N=tinyuf2-adafruit_magtag_29gray-0.20.1.zip
if [[ ! -f "$N" ]]; then
    wget https://github.com/adafruit/tinyuf2/releases/download/0.20.1/$N
fi
if [[ ! -f combined.bin ]]; then
    unzip $N combined.bin
fi
