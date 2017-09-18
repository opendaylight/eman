#!/usr/bin/python
# Title:         eoDeviceHTTPSim.py
# Description:   Simple httpServer to run on localhost or mininet host and return sample ENERGY_OBJECT JSON 
#                default prints a response as string, paths access sample files in mininet custom folder
# Auther:        Frank Sandoval - Pajarito Technologies LLC
# Created:       12/9/2016 as EOserver.oy
#                5/12/17 added full eoDevice representation 
# version:       0.1
# usage:         $ python eoDeviceHTTPSim.py &
#                test in separate terminal: $curl 127.0.0.1:9000
#                $ kill %python
# license        none
# python version        2.7.6   
#
import time
import BaseHTTPServer
import json
import random
import urlparse
import re
import sys, getopt

HOST_NAME = '127.0.0.1'
PORT_NUMBER = 9000 
INFILE = 'eoDeviceMeasures.json'

# Simulator models a single eoDevice with either one or two energyObjects, configured by oeObjectCount. 
# oeObjectCount can be set POST with query string "eoObjectCount=x", where x = 0 will set eoObjectCount to 0,
# any other value will set it to 2
# The value of eoPower can be configured by a POST with query string "powerLevel=high/low". In a single
# eoObject configuration, the object's eoPower will be set to the high or low power setting. In a 
# two eoObject config, one of the two object's eoPower will be set. This crudely models a device, such
# as a CCAP device, that allows for individual components like line cards to be powered down

class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(s):
        """Respond to a GET request."""
        s.send_response(200)
        s.send_header("Content-type", "application/json")
        s.end_headers()
                
        eoDevice1 = { "eoDevices" : { "eoDeviceEntries" : [   
            { 
                "name" : "eoDeviceEntry1",
                "eoCategory" : "consumer",
                "index" : 0,
                "name" : "name",
                "identifier" : 0,
                "alternateKey" : "null",
                "domainName" : "domain",
                "role" : "consume",
                "keyWords" : "null",
                "importance" : 1,
                "multiplier" : 0,
                "caliber" : "actual",
                "accuracy" : 100,
                "value" : "50",
                "units" : "W"
            }
            ]
        }}
        
        if s.path=="/default":
            s.wfile.write(json.dumps(eoDevice1))
        else:
            print "eoDeviceHTTPSim do_GET: inFile = " + inFile
            f = open(inFile)
            s.wfile.write(f.read())


def main(argv):
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    
    global inFile

    print 'ARGV      :', sys.argv[1:]
    try:
        opts, args = getopt.getopt(argv,"i:",["inFile="])
#        opts, args = getopt.getopt(argv,"hi:o:",["inFile=","powerLevel="])
        print 'OPTIONS   :', opts
    except getopt.GetoptError:
        print 'eoDeviceHTTPSim.py error: -i <inFile> -p <powerLevel>'
        sys.exit(2)
    
    inFile = INFILE
    for opt, arg in opts:
        if opt == '-h':
            print 'eoDeviceHTTPSim.py usage: -i <inFile> -p <powerLevel>'
            sys.exit()
        elif opt in ("-i", "--inFile"):
            inFile = arg
            print 'infile should be arg ', inFile, arg
    
    print "eoDeviceHTTPSim main: inFile = " + inFile
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)

        
if __name__ == '__main__':
    main(sys.argv[1:])
    