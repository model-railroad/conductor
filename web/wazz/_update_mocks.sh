#!/usr/bin/bash
set -e
cd public
curl --output mock_live.json https://r.alfray.com/cgi/dazz/live.json
curl --output mock_perf.json https://r.alfray.com/cgi/dazz/perf.json
curl --output mock_statz https://r.alfray.com/cgi/dazz/statz
