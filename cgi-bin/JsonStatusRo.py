#!/usr/bin/python2
import cgi
import os
import sys
import time

# This is a fork of JsonStatusRw.py that only supports reading the data.

# Compatibility requirement: Python 2.5.2 support.
# Avoid: with.

# JSON syntax:
# - A 2-level dictionary.
# - Keys either contain a tuple (ts, value) or a sub-dictionary.
# - Value is optional, e.g. (ts,) or (ts, value) is accepted.
# - All terminal values must be strings.

# apt-get install python-simplejson
try:
    import json
except ImportError:
    import simplejson as json

_DEBUG = False

FILE = "rtac_json_data.txt"

ALLOWED_KEYS = ["ts", "value"]

def file_path():
    """Returns file path for data or None. The file must exist, otherwise None is returned."""
    _dir = os.environ.get("DOCUMENT_ROOT", "")
    _dir = _dir.replace("public_html","apache-settings")
    if _DEBUG: print("\n\n_dir: :", _dir)

    if not os.path.isdir(_dir):
        return None

    _file = os.path.join(_dir, FILE)
    if _DEBUG: print("\n\n_file: :", _file)
    if not os.path.isfile(_file):
        return None
    return _file

def sanitize_data(data):
    if isinstance(data, dict):
        return sanitize_keys(2, data)
    return {}

def sanitize_keys(level, data):
    if level < 0:
        return {}
    level -= 1
    remove = []
    if "ts" in data:
        # This is a (ts) or (ts,value) dict.
        # Remove everything that is not one of the expected keys.
        # All values must be string. Remove otherwise.
        for k,v in data.iteritems():
            if (not k in ALLOWED_KEYS) or (not isinstance(v, (str, unicode))):
                remove.append(k)
    else:
        # This must be a dictionary. All keys must be sub-dicts.
        # Remove everything that is different.
        for k,v in data.iteritems():
            if isinstance(v, dict):
                sanitize_keys(level, v)
            else:
                remove.append(k)
    for k in remove:
        del data[k]
    return data

def load_data():
    """Returns existing data as dict."""
    _file = file_path()
    f = None
    try:
        f = open(_file)
        return sanitize_data(json.load(f))
    except e:
        if _DEBUG: raise e
        return {}
    finally:
        if f: f.close()

def doCgi():
    _exit = 1
    try:
        form = cgi.FieldStorage()

        if _DEBUG:
            print("Content-type: text/html")
            print("")
            sys.stderr = sys.stdout
            cgi.print_directory()
            cgi.print_arguments()
            cgi.print_environ(os.environ)
            print("\n\nmethod: :", os.environ.get("REQUEST_METHOD", ""))
            print("\n\njson: :", form.value)

        if os.environ.get("REQUEST_METHOD", "") == "GET":
            existing = load_data()
            print("Content-Type: application/json")
            print("Status: 200 It really tied the room together")
            print("")
            print(json.dumps(existing, indent=1))
            _exit = 0
            return

        print("Status: 400 That is just like your opinion man")
        print("")

    except:
        print("Status: 500 Princess is in another castle")
        print("")
        if _DEBUG:
            cgi.print_exception()
    finally:
        sys.exit(_exit)

doCgi()

