#!/usr/bin/python
# Title:         eoDeviceHTTPSim.py
# Description:   Simple httpServer to run on localhost or mininet host and return sample ENERGY_OBJECT JSON 
#                default prints a response as string, paths access sample files in mininet custom folder
# Auther:        Frank Sanodval
# Created:       12/9/2016 as EOserver.oy
#                5/12/17 added full eoDevice representation 
# version:       0.1
# usage:         from mininet prompt: h1 python custom/eoDeviceHTTPSim.py &
#                from mininet prompt: h2 curl 10.0.0.1
#                from mininet prompt: h1 kill %python
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

# model power levels in eoDevice
powerLevel = 40
highPower = 40
lowPower = 10
totalPower = highPower + lowPower

# An eoDevice contains a list of eoObjects
eoObjCount = 1

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

        eoPower = str(powerLevel + random.randint(0, 10))
                
        eoDevice1 = {
            'components' : [
                {'eoPowerMeasurement' : {
                    'timeStamp': "0",
                    'eoPower': eoPower,
                    'eoPowerNameplate': "22",
                    'eoPowerUnitMultiplier': "22",
                    'eoPowerAccuracy': "22",
                    'eoPowerMeasurementCaliber': "22",
                    'eoPowerCurrentType': "22",
                    'eoPowerMeasurementLocal': "22",
                    'eoPowerAdminState': "22",
                    'eoPowerOperState': "22",
                    'eoPowerStateEnterReason': "22"
                }}
           ]
        }
        
        eoDevice2 = {
            'components' : [
                { 'eoPowerMeasurement' : {
                    'timeStamp': "0",
                    'eoPower': eoPower,
                    'eoPowerNameplate': "22",
                    'eoPowerUnitMultiplier': "22",
                    'eoPowerAccuracy': "22",
                    'eoPowerMeasurementCaliber': "22",
                    'eoPowerCurrentType': "22",
                    'eoPowerMeasurementLocal': "22",
                    'eoPowerAdminState': "22",
                    'eoPowerOperState': "22",
                    'eoPowerStateEnterReason': "22"
                }},
                {'eoPowerMeasurement' : {
                    'timeStamp': "0",
                    'eoPower': highPower,
                    'eoPowerNameplate': "22",
                    'eoPowerUnitMultiplier': "22",
                    'eoPowerAccuracy': "22",
                    'eoPowerMeasurementCaliber': "22",
                    'eoPowerCurrentType': "22",
                    'eoPowerMeasurementLocal': "22",
                    'eoPowerAdminState': "22",
                    'eoPowerOperState': "22",
                    'eoPowerStateEnterReason': "22"
                }}
           ]
        }

        if eoObjCount==1:
            eoDevice = eoDevice1
        else:
            eoDevice = eoDevice2

        if s.path=="/EO":
            f = open(INFILE)
            s.wfile.write(f.read())
        else:
#            print json.dumps(eoDevice)
            s.wfile.write(json.dumps(eoDevice))
#            d = eoDevice['energyObject']
#            print d['eoPower']

    def do_POST(s):
        global powerLevel
        global eoObjCount
        
#        print "post: path = " + re.escape(s.path)

        s.send_response(200)
        # Doesn't do anything with posted data
#        s.wfile.write("<html><body><h1>POST!</h1></body></html>")
        
#        url = 'http://localhost:9000/?objCount=2&powerLevel=high'
#        par = urlparse.parse_qs(urlparse.urlparse(url).query)

#        print par['objCount'], par['powerLevel']
#        for k, v in par.items():
#            print(k, v)
#            print

        par = urlparse.parse_qs(urlparse.urlparse(s.path).query)

        if 'objCount' in par:
            ls = par['objCount']
            print "ls objCount: " + ls[0]
            if ls[0] == "1":
                eoObjCount = 1
            else:
                eoObjCount = 2
        else:
            print "objCount not in query params"
            
        if 'powerLevel' in par:
            ls = par['powerLevel']
            print "ls powerLevel: " + ls[0]
            if ls[0] == "high":
                powerLevel = 40
            else:
                powerLevel = 10
        else:
            print "powerLevel not in query params"
            
         # toggle power level
#        global totalPower        
#        powerLevel = totalPower - powerLevel
        print "eoDeviceHTTPSim do_POST: oeObjectCount " + str(eoObjCount) + " powerLevel " + str(powerLevel)

def main(argv):
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
#    print "eoDeviceHTTPSim on: "
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    
    global powerLevel
    global eoObjCount

    try:
        opts, args = getopt.getopt(argv,"hi:o:",["eoObjCount=","powerLevel="])
    except getopt.GetoptError:
        print 'eoDeviceHTTPSim.py error: -e <eoObjectCount> -p <powerLevel>'
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print 'eoDeviceHTTPSim.py usage: -e <eoObjectCount> -p <powerLevel>'
            sys.exit()
        elif opt in ("-e", "--eoObjCount"):
            eoObjCount = arg
        elif opt in ("-p", "--powerLevel"):
            powerLevel = arg
    
    print "eoDeviceHTTPSim main: oeObjectCount " + str(eoObjCount) + " powerLevel " + str(powerLevel)
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)

        
if __name__ == '__main__':
    main(sys.argv[1:])
    