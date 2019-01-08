#!/usr/bin/python
import cgi
import os
import urllib2
import sys
import time

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
_LOCAL = False

FILE = "rtac_json_data.txt"
RORW = ("ro", "rw")

ALLOWED_KEYS = ["ts", "value"]

def file_path():
    """Returns file path for data or None. The file must exist, otherwise None is returned."""
    _dir = os.environ.get("DOCUMENT_ROOT", "")
    _dir = _dir.replace("public_html","apache-settings")
    if not os.path.isdir(_dir):
        return None

    _file = os.path.join(_dir, FILE)
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
    except:
        return {}
    finally:
        if f: f.close()

def merge_data(existing, new_data):
    """Returns merged data as dict."""
    for k,v in new_data.iteritems():
        merge_key(existing, k, v)
    return existing

def merge_key(existing, key, value):
    if not key in existing:
        existing[key] = value
    elif type(existing[key]) == type(value):
        if isinstance(value, dict):
            for k,v in value.iteritems():
                merge_key(existing[key], k, v)
        elif isinstance(value, (str, unicode)):
            existing[key] = value

def write_data(new_data):
    """Writes data.
    The file MUST EXIST even for it to be written the first time."""
    _file = file_path().replace(*RORW)
    if not os.path.isfile(_file):
        raise "File not written as it is not present."
    f = None
    try:
        f = open(_file, "w")
        json.dump(new_data, f, indent=1)
    finally:
        if f: f.close()

def doCgi():
    _exit = 1
    global _LOCAL
    try:
        form = cgi.FieldStorage()

        if _DEBUG:
            print "Content-type: text/html"
            print
            sys.stderr = sys.stdout
            cgi.print_directory()
            cgi.print_arguments()
            cgi.print_environ(os.environ)
            print "\n\nmethod: :", os.environ.get("REQUEST_METHOD", "")
            print "\n\njson: :", form.value
        else:
            _LOCAL = os.environ.get("AUTH_TYPE", "") == "Basic" \
                and os.environ.get("REMOTE_ADDR", "").startswith("192.168.1.") \
                and os.environ.get("REMOTE_HOST", "").startswith(os.environ.get("REMOTE_USER", ""))

        if os.environ.get("REQUEST_METHOD", "") == "POST":
            new_data = sanitize_data(json.loads(form.value))
            existing = load_data()
            new_data = merge_data(existing, new_data)
            write_data(new_data)

            print "Status: 200 I like what you got"
            print
            _exit = 0
            return
        elif os.environ.get("REQUEST_METHOD", "") == "GET":
            existing = load_data()
            print "Content-Type: application/json"
            print "Status: 200 It really tied the room together"
            print
            print json.dumps(existing, indent=1)
            _exit = 0
            return

        print "Status: 400 That is just like your opinion man"
        print

    except:
        print "Status: 500 Princess is in another castle"
        print
        if _DEBUG or _LOCAL:
            cgi.print_exception()
    finally:
        sys.exit(_exit)

doCgi()
