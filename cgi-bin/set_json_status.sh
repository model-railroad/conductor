#!/bin/bash
set -e

PROG="$0"

function error() {
    echo
    echo "Error: $*"
    echo
    echo "Usage: $PROG [server-url|file] computer [on|off]"
    exit 1
}

URL="$1"
if [[ -f "$URL" ]]; then
    URL=$(cat "$URL")
fi
if [[ -z "$URL" ]]; then
    error "Missing URL or '@file' argument pointing to a text containing the server URL."
fi

KEY="$2"
if [[ ! "$KEY" =~ ^computer* ]]; then
    error "Key (2nd argument) must start with 'computer' for now."
fi

VAL="$3"
if [[ "$VAL" != "on" && "$VAL" != "off" ]]; then
    error "Value (3rd argument) must be either 'on' or 'off'."
fi

# Get the date in ISO-8601 format.
# Alternative example: date +%Y-%m-%dT%H:%M:%S%:z
TS=$(date --iso-8601=seconds)

TYPE="Content-Type: text/plain"
JSON="{ \"$KEY\": { \"value\": \"$VAL\" , \"ts\": \"$TS\" } }"

if [[ -x $(which curl) ]]; then
    curl --header "$TYPE" --data "$JSON" "$URL"
else
    wget --quiet --output-document - --header "$TYPE" --post-data "$JSON" "$URL"
fi

echo $JSON
