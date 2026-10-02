#!/usr/bin/python3

# A no-op dummy version of cgi/rtac_status.py.
# The functionality is deprecated.
# This does nothing and just returns 200 to help transitioning older Conductor clients.

def doCgi():
    print("Content-Type: application/json")
    print("Status: 200 It really tied the room together")
    print("")
    print("{}")

doCgi()

