#!/usr/bin/python

import getopt
import sys
import urllib
import BaseHTTPServer

VERBOSE = False
HOST = "localhost"
PORT = 20004

JMRI_HOST = "localhost"
JMRI_JSON_PORT = "2056"
JMRI_WEB_PORT = "12080"

SVG_ROOT = "../src/test/resources/v2"
MAPS = {
    "/map1": ( "turnouts.html", "Conductor Map Mainline 1.svg" )
}

def usage():
    print """Web server for turnouts.

Syntax :
   [-v | --verbose] [-i | -h | --ip | --host IP] [-p | --port PORT] [--help]

Default host/port is %s:%s.
""" % (HOST, PORT)
    sys.exit(2)

class Handler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_HEAD(s):
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()

    def do_GET(self):
        """Respond to a GET request."""
        # self.path contains the /path part of the request
        print "Request:", self.path
        
        if self.path in MAPS:
            self.serve_map(MAPS[self.path])
        elif self.path.endswith(".svg"):
            self.serve_svg(self.path)
        else:
            self.serve_index()

    def serve_svg(self, path):
        self.send_response(200)
        self.send_header("Content-type", "text/xml")
        self.end_headers()
        path = urllib.unquote(path).replace("..", "")
        with open(SVG_ROOT + path) as f: self.wfile.write(f.read())

    def serve_map(self, map):
        html = ""
        with open(map[0]) as f: html = f.read()
        html = html.replace("@_JMRI_HOST_@", JMRI_HOST)
        html = html.replace("@_JMRI_JSON_PORT_@", JMRI_JSON_PORT)
        html = html.replace("@_JMRI_WEB_PORT_@", JMRI_WEB_PORT)
        html = html.replace("@_PATH_TO_SVG_@", map[1])

        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(html)
        
    
    def serve_index(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write("<html><head><title>Turnouts Server.</title></head>")
        self.wfile.write("<body><p>Available maps:</p>")
        for path in MAPS:
            self.wfile.write("<p><a href='%s'>%s</a></p>" % (path, path))
        self.wfile.write("</body></html>")

def keep_running():
    return True
        
def run_server():
    http = BaseHTTPServer.HTTPServer((HOST, PORT), Handler)
    print "Serving on http://%s:%s" % (HOST, PORT)
    while keep_running():
        try:
            http.handle_request()
        except KeyboardInterrupt:
            print "Ctrl C; closing."
            sys.exit(0)
    
def main(argv):
    show_help = False
    try:
        opts, args = getopt.getopt(argv, "vni:h:p:", [ "verbose", "help", "ip=", "host=", "port=" ])

        if len(args) > 0:
            print "Unkown parameters:", args
            show_help = True
        
        for opt, arg in opts:
            if opt in [ "-v", "--verbose" ]:
                global VERBOSE
                VERBOSE = True
            elif opt in [ "-i", "-h", "--ip", "--host" ]:
                global HOST
                HOST = arg
            elif opt in [ "-p", "--port" ]:
                global PORT
                PORT = int(arg)
            elif opt in [ "--help" ]:
                show_help = True

        if show_help:
            usage()

        run_server()

    except getopt.GetoptError:
        usage()


if __name__ == "__main__":
    main(sys.argv[1:])

